package dev.runabout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

class RunaboutServiceImpl<T extends JsonObject> implements RunaboutService<T> {

    private final String projectName;
    private final boolean excludeSuper;
    private final Consumer<Throwable> throwableConsumer;
    private final Supplier<Method> callerSupplier;
    private final RunaboutSerializer customSerializer;
    private final Supplier<T> jsonFactory;
    private final Supplier<Timestamp> datetimeSupplier;
    private final Function<Method, String> methodToStringFunction;
    private final RunaboutAPI emitter;

    private final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();

    RunaboutServiceImpl(String projectName, boolean excludeSuper, Consumer<Throwable> throwableConsumer,
                        Supplier<Method> callerSupplier, RunaboutSerializer customSerializer, Supplier<T> jsonFactory,
                        Supplier<Timestamp> datetimeSupplier, Function<Method, String> methodToStringFunction,
                        RunaboutAPI emitter) {
        this.projectName = projectName;
        this.throwableConsumer = throwableConsumer;
        this.excludeSuper = excludeSuper;
        this.callerSupplier = callerSupplier;
        this.customSerializer = customSerializer;
        this.jsonFactory = jsonFactory;
        this.datetimeSupplier = datetimeSupplier;
        this.methodToStringFunction = methodToStringFunction;
        this.emitter = emitter;
    }

    @Override
    public RunaboutInstance serialize(Object object) {

        // Short circuit if object is null.
        if (object == null) {
            return DefaultSerializer.getNullInput();
        }

        RunaboutInstance instance;

        instance = invokeInstanceSerializer(object);

        if (instance == null) {
            instance = invokeSafe(this.customSerializer, object);
        }

        if (instance == null) {
            instance = invokeSafe(o -> defaultSerializer.toRunaboutGenericRecursive(o, this::serialize), object);
        }

        return Optional.ofNullable(instance).orElseGet(DefaultSerializer::getEmptyInput);
    }

    @Override
    public T createScenario(final String eventId, final T properties, final Object... objects) {

        final Method method = Objects.requireNonNull(callerSupplier.get(),
                "RunaboutService unable to determine caller method.");

        final T json = jsonFactory.get();

        json.put(RunaboutConstants.VERSION_KEY, RunaboutConstants.JSON_CONTRACT_VERSION);
        json.put(RunaboutConstants.DATETIME_KEY, datetimeSupplier.get().toString());
        json.put(RunaboutConstants.PROJECT_KEY, projectName);
        json.put(RunaboutConstants.EVENT_ID_KEY, eventId);
        json.put(RunaboutConstants.PROPERTIES_KEY, properties);
        json.put(RunaboutConstants.METHOD_KEY, methodToStringFunction.apply(method));

        final List<JsonObject> instances = new ArrayList<>();
        for (final Object object: objects) {
            final RunaboutInstance instance = serialize(object);
            final T instanceJson = jsonFactory.get();
            final String type = getTypeSafe(object);
            instanceJson.put(RunaboutConstants.TYPE_KEY, type);
            instanceJson.put(RunaboutConstants.EVAL_KEY, instance.getEval());
            instanceJson.put(RunaboutConstants.DEPENDENCIES_KEY, String.class, new ArrayList<>(instance.getDependencies()));
            instances.add(instanceJson);
        }

        json.put(RunaboutConstants.INSTANCES_KEY, JsonObject.class, instances);
        return json;
    }

    @Override
    public void emitScenario(String eventId, T properties, final Object... objects) {
        final T json = createScenario(eventId, properties, objects);
        emitter.queueEmission(json);
    }

    private RunaboutInstance invokeInstanceSerializer(final Object object) {
        RunaboutInstance instance;
        Class<?> clazz = object.getClass();
        instance = invokeInstanceSerializer(object, clazz);
        while (!this.excludeSuper & instance == null && clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            instance = invokeInstanceSerializer(object, clazz);
        }
        return instance;
    }

    private RunaboutInstance invokeInstanceSerializer(final Object object, final Class<?> clazz) {

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

    private RunaboutInstance invokeSafe(final Method method, final Object object) {
        RunaboutInstance instance = null;

        try {
            method.setAccessible(true);
            final RunaboutInstance tempInput = (RunaboutInstance) method.invoke(object);

            if (validInput(tempInput)) {
                instance = tempInput;
            }

        } catch (InvocationTargetException e) {
            throwableConsumer.accept(e.getCause() != null ? e.getCause() : e);
        } catch (Throwable t) {
            throwableConsumer.accept(t);
        }

        return instance;
    }

    private RunaboutInstance invokeSafe(final RunaboutSerializer serializer, final Object o) {
        RunaboutInstance instance = null;

        if (serializer != null) {
            try {
                final RunaboutInstance tempInput = serializer.toRunaboutGeneric(o);

                if (validInput(tempInput)) {
                    instance = tempInput;
                }

            } catch (Throwable ex) {
                throwableConsumer.accept(ex);
            }
        }

        return instance;
    }

    private static boolean validInput(final RunaboutInstance instance) {
        return instance != null && instance.getEval() != null && !instance.getEval().isEmpty() &&
                instance.getDependencies() != null;
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
}
