package dev.runabout;

import dev.runabout.annotations.Nullable;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * A builder for creating a RunaboutService.
 */
public class RunaboutServiceBuilder {

    //
    // Setter/ServiceLoader fields.
    //
    private MethodResolver methodResolver;
    private RunaboutSerializer customSerializer;
    private Supplier<JsonObject> jsonFactory;
    private RunaboutListener listener;
    private RunaboutApi runaboutApi;

    //
    // Constructor fields.
    //
    private final String projectName;

    /**
     * Creates a new RunaboutServiceBuilder with the given JSON object factory.
     * The supplier should create new instances of JSON objects.
     *
     * @param projectName The name of the project to log scenario under.
     */
    public RunaboutServiceBuilder(final String projectName) {
        this.projectName = Objects.requireNonNull(projectName, "Runabout project name is required.");
    }

    public RunaboutServiceBuilder setRunaboutApi(final RunaboutApi runaboutApi) {
        this.runaboutApi = runaboutApi;
        return this;
    }

    /**
     * Sets the JSON object factory for the RunaboutService.
     * The supplier should create new instances of JSON objects.
     * By default, the service will use {@link JsonObjectImpl::new}.
     *
     * @param jsonFactory The JSON object factory.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder setJsonFactory(final Supplier<JsonObject> jsonFactory) {
        this.jsonFactory = jsonFactory;
        return this;
    }

    /**
     * Sets the caller supplier for the RunaboutService.
     * The supplier should return the desired method for Runabout replay debugging from the stack a runtime.
     * By default, the {@link RunaboutService} will use {@link MethodResolverImpl}.
     *
     * @param methodResolver A supplier that returns the desired {@link Method} to include in the Runabout JSON.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder setMethodResolver(final MethodResolver methodResolver) {
        this.methodResolver = Objects.requireNonNull(methodResolver, "Caller supplier cannot be null.");
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
    public RunaboutServiceBuilder setCustomSerializer(final RunaboutSerializer customSerializer) {
        this.customSerializer = Objects.requireNonNull(customSerializer, "Custom serializer cannot be null.");
        return this;
    }

    // TODO
    public RunaboutServiceBuilder setListener(final RunaboutListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Builds the RunaboutService.
     *
     * @return The RunaboutService.
     */
    public RunaboutService build() {

        final RunaboutApi runaboutApiFinal = resolveService(runaboutApi, RunaboutApi.class)
                .orElseThrow(() -> new IllegalArgumentException("Runabout API is required."));

        final MethodResolver methodResolverFinal = resolveService(methodResolver, MethodResolver.class)
                .orElseGet(MethodResolverImpl::new);

        final Supplier<JsonObject> jsonFactory = Optional.ofNullable(this.jsonFactory)
                .orElse(JsonObjectImpl::new);

        final RunaboutSerializer customSerializerFinal = resolveService(customSerializer, RunaboutSerializer.class)
                .orElse(null);;

        final RunaboutListener listenerFinal = resolveService(listener, RunaboutListener.class)
                .orElse(null);

        return new RunaboutServiceImpl(projectName, runaboutApiFinal, methodResolverFinal, listenerFinal,
                customSerializerFinal, jsonFactory);
    }

    private static <T> Optional<T> resolveService(final T input, final Class<T> service) {
        return Optional.ofNullable(input).or(() -> ServiceLoader.load(service).findFirst());
    }
}
