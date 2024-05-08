package dev.runabout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private final Supplier<String> datetimeSupplier;
    private final Function<Method, String> methodToStringFunction;
    private final RunaboutEmitter emitter;

    private final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();

    RunaboutServiceImpl(String projectName, boolean excludeSuper, Consumer<Throwable> throwableConsumer,
                        Supplier<Method> callerSupplier, RunaboutSerializer customSerializer, Supplier<T> jsonFactory,
                        Supplier<String> datetimeSupplier, Function<Method, String> methodToStringFunction,
                        RunaboutEmitter emitter) {
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
    public void emit(String eventId, T properties, T scenario) {
        final T json = jsonFactory.get();
        json.put(RunaboutConstants.EVENT_ID_KEY, eventId);
        json.put(RunaboutConstants.PROJECT_NAME_KEY, projectName);
        json.put(RunaboutConstants.PROPERTIES_KEY, properties);
        json.put(RunaboutConstants.SCENARIO_KEY, scenario);
        final String content = json.toJson();
        emitter.queueEmission(content);
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
    public Method getCallerMethod() {
        return callerSupplier.get();
    }

    @Override
    public T toRunaboutJson(Method method, Object... objects) {

        Objects.requireNonNull(method, "RunaboutService unable to determine caller method.");

        final T json = jsonFactory.get();

        // Put version data in json.
        json.put(RunaboutConstants.VERSION_KEY, RunaboutProperties.getInstance().getJsonContractVersion());

        // Put method data in json.
        json.put(RunaboutConstants.METHOD_KEY, methodToStringFunction.apply(method));

        // Put datetime data in json.
        json.put(RunaboutConstants.DATETIME_KEY, datetimeSupplier.get());

        final List<JsonObject> inputs = new ArrayList<>();
        for (final Object object: objects) {
            final RunaboutInput input = serialize(object);
            final T inputJson = jsonFactory.get();
            final String type = getTypeSafe(object);
            inputJson.put(RunaboutConstants.TYPE_KEY, type);
            inputJson.put(RunaboutConstants.EVAL_KEY, input.getEval());
            inputJson.put(RunaboutConstants.DEPENDENCIES_KEY, String.class, new ArrayList<>(input.getDependencies()));
            inputs.add(inputJson);
        }

        json.put(RunaboutConstants.INPUTS_KEY, JsonObject.class, inputs);
        return json;
    }

    private RunaboutInput invokeInstanceSerializer(final Object object) {
        RunaboutInput input;
        Class<?> clazz = object.getClass();
        input = invokeInstanceSerializer(object, clazz);
        while (!this.excludeSuper & input == null && clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            input = invokeInstanceSerializer(object, clazz);
        }
        return input;
    }

    private RunaboutInput invokeInstanceSerializer(final Object object, final Class<?> clazz) {

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
            throwableConsumer.accept(e.getCause() != null ? e.getCause() : e);
        } catch (Throwable t) {
            throwableConsumer.accept(t);
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
                throwableConsumer.accept(ex);
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
}
