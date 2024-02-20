package dev.runabout;

import dev.runabout.json.JsonFactory;
import dev.runabout.json.JsonObject;

import java.util.Optional;
import java.util.Set;

public class RunaboutServiceBuilder<T extends JsonObject> {

    private CallerSupplier callerSupplier;
    private Set<Class<?>> callerClassBlacklist;
    private RunaboutSerializer customSerializer;
    private boolean shouldThrow;
    private boolean excludeSuper;

    private final JsonFactory<T> jsonFactory;

    public static RunaboutServiceBuilder<JsonObject> getDefaultBuilder() {
        return  new RunaboutServiceBuilder<>(new JsonObjectImpl.JsonFactoryImpl());
    }

    public RunaboutServiceBuilder(JsonFactory<T> jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public RunaboutServiceBuilder<T> setCallerSupplier(CallerSupplier callerSupplier) {
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
        final CallerSupplier callerSupplierFinal = Optional.ofNullable(callerSupplier)
                .orElseGet(() -> new DefaultCallerSupplier(callerClassBlacklistFinal));

        final RunaboutSerializer customSerializerFinal = Optional.ofNullable(this.customSerializer)
                .orElseGet(RunaboutSerializer::getSerializer);

        return new RunaboutServiceImpl<>(shouldThrow, excludeSuper, callerSupplierFinal, customSerializerFinal,
                jsonFactory);
    }
}
