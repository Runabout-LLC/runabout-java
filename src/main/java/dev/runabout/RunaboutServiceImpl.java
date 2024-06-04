package dev.runabout;

import dev.runabout.annotations.Nullable;
import dev.runabout.annotations.RunaboutEnabled;
import dev.runabout.annotations.RunaboutParameter;
import dev.runabout.annotations.ToRunabout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class RunaboutServiceImpl implements RunaboutService {

    private final String projectName;
    private final RunaboutApi runaboutApi;
    private final MethodResolver methodResolver;
    private final RunaboutListener listener;
    private final RunaboutSerializer customSerializer;

    private final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();

    RunaboutServiceImpl(String projectName,
                        RunaboutApi runaboutApi,
                        MethodResolver methodResolver,
                        RunaboutListener listener,
                        RunaboutSerializer customSerializer) {
        this.projectName = projectName;
        this.methodResolver = methodResolver;
        this.customSerializer = customSerializer;
        this.runaboutApi = runaboutApi;
        this.listener = listener;
    }

    @Override
    public RunaboutInput serialize(Object object) {

        // Short circuit if object is null.
        if (object == null) {
            return DefaultSerializer.getNullInput();
        }

        RunaboutInput input;

        input = invokeInstanceSerializer(object);

        if (input == null) {
            input = invokeSafe(this.customSerializer, object);
        }

        if (input == null) {
            input = invokeSafe(o -> defaultSerializer.toRunaboutGenericRecursive(o, this::serialize), object);
        }

        return Optional.ofNullable(input).orElseGet(DefaultSerializer::getEmptyInput);
    }

    @Override
    public RunaboutScenario createScenario(final String eventId, final JsonObject properties, final Object... objects) {

        final Timestamp datetime = getDatetime();
        final String method = methodResolver.getSerializedMethod();

        final List<RunaboutInstance> instances = new ArrayList<>();
        for (final Object object: objects) {
            final RunaboutInput input = serialize(object);
            final String type = getTypeSafe(object);
            final RunaboutInstance instance = RunaboutInstance.of(type, input);
            instances.add(instance);
        }

        return new RunaboutScenario(method, eventId, projectName, datetime, properties, instances);
    }

    @Override
    public void saveScenario(String eventId, JsonObject properties, Object... objects) {
        final RunaboutScenario scenario = createScenario(eventId, properties, objects);
        runaboutApi.ingestScenario(scenario);
    }

    private RunaboutInput invokeInstanceSerializer(final Object object) {

        Class<?> clazz = object.getClass();

        //
        // Try RunaboutEnabled annotated constructors
        //
        RunaboutInput input = invokeRunaboutEnabledSerializer(object, clazz);

        //
        // Try ToRunabout annotated methods.
        //
        if (input == null) {
            input = invokeInstanceToRunaboutSerializer(object, clazz);
            while (input == null && clazz.getSuperclass() != null) {
                clazz = clazz.getSuperclass();
                input = invokeInstanceToRunaboutSerializer(object, clazz);
            }
        }

        return input;
    }

    // TODO attempt to use super?
    @Nullable
    private RunaboutInput invokeRunaboutEnabledSerializer(final Object object, final Class<?> clazz) {

        RunaboutInput input = null;
        final Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                .filter(c -> c.isAnnotationPresent(RunaboutEnabled.class))
                .findFirst().orElse(null);

        if (constructor != null) {

            class ParameterField {
                private final String name;
                private final Class<?> type;

                public ParameterField(String name, Class<?> type) {
                    this.name = name;
                    this.type = type;
                }

                @Override
                public boolean equals(Object object) {
                    if (this == object) return true;
                    if (object == null || getClass() != object.getClass()) return false;
                    ParameterField that = (ParameterField) object;
                    return Objects.equals(name, that.name) && Objects.equals(type, that.type);// TODO make type equality more nuanced
                }

                @Override
                public int hashCode() {
                    return Objects.hash(name);
                }
            }

            final List<ParameterField> parameterFields = Arrays.stream(constructor.getParameters())
                    .map(parameter -> new ParameterField(parameter.isAnnotationPresent(RunaboutEnabled.class) ?
                            parameter.getAnnotation(RunaboutParameter.class).value() : parameter.getName(), parameter.getType()))
                    .collect(Collectors.toList());

            final Map<ParameterField, Integer> parameterFieldMap = new HashMap<>();
            for (int i = 0; i < parameterFields.size(); i++) {
                parameterFieldMap.put(parameterFields.get(i), i);
            }

            final List<Field> fields = new ArrayList<>(parameterFields.size());
            Arrays.stream(clazz.getDeclaredFields())
                    .takeWhile(f -> !parameterFieldMap.isEmpty())
                    .forEach(f -> {
                        final ParameterField pf = new ParameterField(f.getName(), f.getType());
                        final Integer index = parameterFieldMap.remove(pf);
                        if (index != null) {
                            fields.add(index, f);
                        }
                    });

            //
            // VALIDATION TODO COMMENT
            //
            if (parameterFieldMap.isEmpty()) {
                final StringBuilder eval = new StringBuilder("new ").append(clazz.getSimpleName()).append("(");
                final Set<String> dependencies = new HashSet<>(Set.of(clazz.getCanonicalName()));

                for (Field field : fields) {

                    Object value;
                    field.setAccessible(true); // Safe because Class.getFields always returns a new copy of the fields.
                    try {
                        value = field.get(object);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    RunaboutInput fieldInput = serialize(value);
                    if (fieldInput == null || fieldInput.getEval() == null || fieldInput.getEval().isEmpty()) {
                        fieldInput = DefaultSerializer.getNullInput();
                    }
                    eval.append(fieldInput.getEval()).append(", ");
                    dependencies.addAll(fieldInput.getDependencies());
                }

                input = RunaboutInput.of(eval.toString(), dependencies);
            }
        }
        return input;
    }

    @Nullable
    private RunaboutInput invokeInstanceToRunaboutSerializer(final Object object, final Class<?> clazz) {

        final Set<Method> methods = Optional.ofNullable(clazz).map(cls -> {
            final Set<Method> set = new HashSet<>(Set.of(cls.getMethods()));
            set.addAll(Set.of(cls.getDeclaredMethods()));
            return set;
        }).orElseGet(Collections::emptySet);

        return methods.stream()
                .filter(method -> method.isAnnotationPresent(ToRunabout.class))
                .findFirst()
                .map(method -> invokeSafe(method, object))
                .orElse(null);
    }

    private RunaboutInput invokeSafe(final Method method, final Object object) {
        RunaboutInput input = null;

        try {
            method.setAccessible(true);
            final RunaboutInput tempInput = (RunaboutInput) method.invoke(object);

            if (validInput(tempInput)) {
                input = tempInput;
            }

        } catch (InvocationTargetException e) {
            listener.onError(e.getCause() != null ? e.getCause() : e);
        } catch (Throwable t) {
            listener.onError(t);
        }

        return input;
    }

    private RunaboutInput invokeSafe(final RunaboutSerializer serializer, final Object o) {
        RunaboutInput input = null;

        if (serializer != null) {
            try {
                final RunaboutInput tempInput = serializer.toRunaboutGeneric(o);

                if (validInput(tempInput)) {
                    input = tempInput;
                }

            } catch (Throwable ex) {
                listener.onError(ex);
            }
        }

        return input;
    }

    private static boolean validInput(final RunaboutInput input) {
        return input != null && input.getEval() != null && !input.getEval().isEmpty() &&
                input.getDependencies() != null;
    }

    private static String getTypeSafe(final Object object) {
        return Optional.ofNullable(object)
                .map(Object::getClass)
                .map(clazz -> clazz.isAnonymousClass() ? getAnonymousImplClass(clazz) : clazz.getCanonicalName())
                .orElse("null");
    }

    private static String getAnonymousImplClass(final Class<?> clazz) {
        return Optional.ofNullable(clazz)
                .map(c -> c.getInterfaces().length > 0 ? c.getInterfaces()[0] : c.getSuperclass())
                .map(Class::getCanonicalName)
                .orElse("null");
    }

    private static Timestamp getDatetime() {
        return Timestamp.from(Instant.now());
    }
}
