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
import java.util.function.Supplier;

class RunaboutServiceImpl<T extends JsonObject> implements RunaboutService<T> {

    private final boolean shouldThrow;
    private final boolean excludeSuper;
    private final Supplier<Method> callerSupplier;
    private final RunaboutSerializer customSerializer;
    private final Supplier<T> jsonFactory;

    private final DefaultSerializer defaultSerializer = DefaultSerializer.getInstance();

    RunaboutServiceImpl(boolean shouldThrow, boolean excludeSuper, Supplier<Method> callerSupplier,
                               RunaboutSerializer customSerializer, Supplier<T> jsonFactory) {
        this.shouldThrow = shouldThrow;
        this.excludeSuper = excludeSuper;
        this.callerSupplier = callerSupplier;
        this.customSerializer = customSerializer;
        this.jsonFactory = jsonFactory;
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

        return Optional.ofNullable(input).orElseGet(() -> RunaboutInput.of("", Collections.emptySet()));
    }

    @Override
    public Method getCallerMethod() {
        return callerSupplier.get();
    }

    @Override
    public T toRunaboutJson(Object... objects) {
        return toRunaboutJson(callerSupplier.get(), objects);
    }

    @Override
    public T toRunaboutJson(Method method, Object... objects) {

        Objects.requireNonNull(method, "RunaboutService unable to determine caller method."); // TODO different ex type

        final T json = jsonFactory.get();

        // Put version data in json.
        json.put(RunaboutConstants.VERSION_KEY, RunaboutProperties.getInstance().getJsonContractVersion());

        // Put method data in json.
        json.put(RunaboutConstants.METHOD_KEY, method.toString());

        final List<JsonObject> inputs = new ArrayList<>();
        for (final Object object: objects) {
            final RunaboutInput input = serialize(object);
            final T inputJson = jsonFactory.get();
            inputJson.put(RunaboutConstants.TYPE_KEY, object.getClass().getCanonicalName());
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
            input = (RunaboutInput) method.invoke(object);
        } catch (IllegalAccessException ex) {
            if (this.shouldThrow) {
                throw new RuntimeException(ex);
            }
        } catch (InvocationTargetException e) {
            if (e.getCause() != null && this.shouldThrow) {
                throw new RuntimeException(e.getCause());
            }
        }

        return input;
    }

    private RunaboutInput invokeSafe(final RunaboutSerializer serializer, final Object o) {
        RunaboutInput input = null;

        if (serializer != null) {
            try {
                input = serializer.toRunaboutGeneric(o);
            } catch (Exception ex) {
                if (this.shouldThrow) {
                    throw ex;
                }
            }
        }

        return input;
    }
}
