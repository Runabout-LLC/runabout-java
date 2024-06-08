package dev.runabout;

import dev.runabout.annotations.Nullable;
import dev.runabout.annotations.RunaboutEnabled;
import dev.runabout.annotations.RunaboutParameter;
import dev.runabout.annotations.ToRunabout;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

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
            input = invokeToRunaboutSerializer(object, clazz);
            while (input == null && clazz.getSuperclass() != null) {
                clazz = clazz.getSuperclass();
                input = invokeToRunaboutSerializer(object, clazz);
            }
        }

        return input;
    }

    @Nullable
    private RunaboutInput invokeRunaboutEnabledSerializer(final Object object, final Class<?> clazz) {
        try {
            RunaboutInput input = null;
            final Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .filter(c -> c.isAnnotationPresent(RunaboutEnabled.class))
                    .findFirst().orElse(null);

            if (constructor != null) {

                final Map<String, List<Integer>> parameterMap = new HashMap<>();
                for (int i = 0; i < constructor.getParameters().length; i++) {
                    final Parameter parameter = constructor.getParameters()[i];
                    if (!parameter.isAnnotationPresent(RunaboutParameter.class)) {
                        throw new RuntimeException("RunaboutEnabled constructor parameters must be " +
                                "annotated with RunaboutParameter. Parameter: [" + parameter.getName() +
                                "] in class: [" + clazz.getCanonicalName() + "] is not annotated.");
                    }
                    parameterMap
                            .computeIfAbsent(parameter.getAnnotation(RunaboutParameter.class).value(),
                                    v -> new ArrayList<>())
                            .add(i);
                }

                final List<Field> fields = new ArrayList<>(parameterMap.size());

                Arrays.stream(clazz.getDeclaredFields())
                        .takeWhile(f -> !parameterMap.isEmpty())
                        .forEach(f -> {
                            final List<Integer> indices = parameterMap.remove(f.getName());
                            if (indices != null) {
                                indices.forEach(index -> fields.add(index, f));
                            }
                        });

                if (parameterMap.isEmpty()) {

                    final StringJoiner joiner = new StringJoiner(", ");
                    final Set<String> dependencies = new HashSet<>(Set.of(clazz.getCanonicalName()));

                    for (Field field : fields) {

                        Object value;
                        field.setAccessible(true);
                        value = field.get(object);
                        RunaboutInput fieldInput = serialize(value);
                        if (fieldInput == null || fieldInput.getEval() == null || fieldInput.getEval().isEmpty()) {
                            fieldInput = DefaultSerializer.getNullInput();
                        }
                        joiner.add(fieldInput.getEval());
                        dependencies.addAll(fieldInput.getDependencies());
                    }

                    final String eval = "new " + clazz.getSimpleName() + "(" + joiner + ")";
                    input = RunaboutInput.of(eval, dependencies);
                }
            }

            return input;
        } catch (Throwable t) {
            listener.onError(t);
            return null;
        }
    }

    @Nullable
    private RunaboutInput invokeToRunaboutSerializer(final Object object, final Class<?> clazz) {

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
