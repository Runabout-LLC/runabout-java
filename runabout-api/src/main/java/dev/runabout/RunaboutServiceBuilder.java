package dev.runabout;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class RunaboutServiceBuilder<T extends JsonObject> {

    private Supplier<Method> callerSupplier;
    private Set<Class<?>> callerClassBlacklist;
    private RunaboutSerializer customSerializer;
    private boolean shouldThrow;
    private boolean excludeSuper;

    private final Supplier<T> jsonFactory;

    public static RunaboutServiceBuilder<JsonObject> getDefaultBuilder() {
        return  new RunaboutServiceBuilder<>(new JsonObjectImpl.JsonFactoryImpl());
    }

    public RunaboutServiceBuilder(Supplier<T> jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public RunaboutServiceBuilder<T> setCallerSupplier(Supplier<Method> callerSupplier) {
        this.callerSupplier = callerSupplier;
        return this;
    }

    public RunaboutServiceBuilder<T> setCallerClassBlacklist(Set<Class<?>> callerClassBlacklist) {
        this.callerClassBlacklist = callerClassBlacklist;
        return this;
    }

    public RunaboutServiceBuilder<T> setCustomSerializer(RunaboutSerializer customSerializer) {
        this.customSerializer = customSerializer;
        return this;
    }

    public RunaboutServiceBuilder<T> setShouldThrow(boolean shouldThrow) {
        this.shouldThrow = shouldThrow;
        return this;
    }

    public RunaboutServiceBuilder<T> setExcludeSuper(boolean excludeSuper) {
        this.excludeSuper = excludeSuper;
        return this;
    }

    public RunaboutService<T> build() {

        if (callerSupplier != null && callerClassBlacklist != null) {
            throw new IllegalArgumentException(); // TODO
        }

        final Set<Class<?>> callerClassBlacklistFinal = Optional.ofNullable(callerClassBlacklist).orElseGet(Set::of);
        final Supplier<Method> callerSupplierFinal = Optional.ofNullable(callerSupplier)
                .orElseGet(() -> new DefaultCallerSupplier(callerClassBlacklistFinal));

        final RunaboutSerializer customSerializerFinal = Optional.ofNullable(this.customSerializer)
                .orElseGet(RunaboutSerializer::getSerializer);

        return new RunaboutServiceImpl<>(shouldThrow, excludeSuper, callerSupplierFinal, customSerializerFinal,
                jsonFactory);
    }
}
