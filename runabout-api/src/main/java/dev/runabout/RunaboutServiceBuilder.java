package dev.runabout;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A builder for creating a RunaboutService.
 *
 * @param <T> the type of Json Object to use.
 */
public class RunaboutServiceBuilder<T extends JsonObject> {

    private Supplier<Method> callerSupplier;
    private Set<Class<?>> callerClassBlacklist;
    private RunaboutSerializer customSerializer;
    private boolean shouldThrow = false;
    private boolean excludeSuper = false;

    private final Supplier<T> jsonFactory;

    /**
     * Gets the default RunaboutServiceBuilder instance which uses the built-in {@link JsonObject} type.
     *
     * @return The default RunaboutServiceBuilder.
     */
    public static RunaboutServiceBuilder<JsonObject> getDefaultBuilder() {
        return  new RunaboutServiceBuilder<>(new JsonObjectImpl.JsonFactoryImpl());
    }

    /**
     * Creates a new RunaboutServiceBuilder with the given JSON object factory.
     * The supplier should create new instances of JSON objects.
     *
     * @param jsonFactory The JSON object factory.
     */
    public RunaboutServiceBuilder(Supplier<T> jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    /**
     * Sets the caller supplier for the RunaboutService.
     * The supplier should return the desired method for Runabout replay debugging from the stack a runtime.
     * By default, the {@link RunaboutService} will use {@link DefaultCallerSupplier}.
     *
     * @param callerSupplier A supplier that returns the desired {@link Method} to include in the Runabout JSON.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder<T> setCallerSupplier(Supplier<Method> callerSupplier) {
        this.callerSupplier = callerSupplier;
        return this;
    }

    /**
     * Sets the caller class blacklist for the RunaboutService.
     * The blacklist is used to filter out classes from the stack trace that should
     * not be included in the Runabout JSON.
     * This method is mutually exclusive with {@link #setCallerSupplier(Supplier)}.
     * By default, the blacklist is empty.
     *
     * @param callerClassBlacklist The set of classes to blacklist from the stack trace.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder<T> setCallerClassBlacklist(Set<Class<?>> callerClassBlacklist) {
        this.callerClassBlacklist = callerClassBlacklist;
        return this;
    }

    /**
     * Sets the custom serializer for the RunaboutService.
     * The serializer is used to serialize objects that do not have an instance serializer method.
     * This serializer instance is not the default serializer, but a custom one.
     * By default, the serializer the first instance of the {@link RunaboutSerializer} service
     * discovered via the ServiceLoader.
     *
     * @param customSerializer The custom serializer to use.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder<T> setCustomSerializer(RunaboutSerializer customSerializer) {
        this.customSerializer = customSerializer;
        return this;
    }

    /**
     * Sets whether the RunaboutService should throw exceptions when it encounters them.
     * By default, the service will not throw exceptions.
     *
     * @param shouldThrow Whether the service should throw exceptions.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder<T> setShouldThrow(boolean shouldThrow) {
        this.shouldThrow = shouldThrow;
        return this;
    }

    /**
     * Sets whether the RunaboutService should not be able to invoke instance serializer methods
     * annotated with {@link ToRunabout} from superclasses.
     * By default, the service will not exclude super classes' instance serializer methods.
     *
     * @param excludeSuper Whether the service should exclude super classes.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder<T> setExcludeSuper(boolean excludeSuper) {
        this.excludeSuper = excludeSuper;
        return this;
    }

    /**
     * Builds the RunaboutService.
     *
     * @return The RunaboutService.
     */
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
