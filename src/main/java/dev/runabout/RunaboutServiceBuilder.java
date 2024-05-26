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
    public RunaboutServiceBuilder(String projectName, RunaboutApi runaboutApi) {
        this.projectName = Objects.requireNonNull(projectName, "Runabout project name is required.");
    }

    public RunaboutServiceBuilder setRunaboutApi(RunaboutApi runaboutApi) {
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
    public RunaboutServiceBuilder setJsonFactory(Supplier<JsonObject> jsonFactory) {
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
    public RunaboutServiceBuilder setMethodResolver(MethodResolver methodResolver) {
        this.methodResolver = Objects.requireNonNull(methodResolver, "Caller supplier cannot be null.");
        return this;
    }
//
//    /**
//     * Sets the caller class blacklist for the RunaboutService.
//     * The blacklist is used to filter out classes from the stack trace that should
//     * not be included in the Runabout JSON.
//     * This method is mutually exclusive with {@link #setMethodResolver(Supplier)}.
//     * By default, the blacklist is empty.
//     *
//     * @param callerClassBlacklist The set of classes to blacklist from the stack trace.
//     * @return The RunaboutServiceBuilder.
//     */
//    public RunaboutServiceBuilder setCallerClassBlacklist(Set<Class<?>> callerClassBlacklist) {
//        this.callerClassBlacklist = Objects.requireNonNull(callerClassBlacklist,
//                "Caller class blacklist cannot be null.");
//        return this;
//    }

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
    public RunaboutServiceBuilder setCustomSerializer(RunaboutSerializer customSerializer) {
        this.customSerializer = Objects.requireNonNull(customSerializer, "Custom serializer cannot be null.");
        return this;
    }

//    /**
//     * Sets the datetime supplier for the RunaboutService.
//     * The supplier should return the current datetime as an {@link Timestamp}.
//     * By default, the supplier will return the current datetime using {@link Instant#now()}.
//     *
//     * @param datetimeSupplier The datetime supplier.
//     * @return The RunaboutServiceBuilder.
//     */
//    public RunaboutServiceBuilder setDatetimeSupplier(Supplier<Timestamp> datetimeSupplier) {
//        this.datetimeSupplier = datetimeSupplier;
//        return this;
//    }

//    /**
//     * Sets the methodToStringFunction used to convert the caller method to a string for the JSON object
//     * in a way that Runabout knows how to read. The expected format is as follows:
//     * Fully qualified classname + "#" + method name + "(" + fully qualified argument types in order
//     * delimited by ", " ... + ")"
//     *
//     * @param methodToStringFunction The methodToString function.
//     * @return The RunaboutServiceBuilder.
//     */
//    public RunaboutServiceBuilder setMethodToStringFunction(Function<Method, String> methodToStringFunction) {
//        this.methodToStringFunction = methodToStringFunction;
//        return this;
//    }
//
//    /**
//     * Sets the stack frame predicate for the RunaboutService.
//     * The predicate is used to filter out stack frames when determining the caller in {@link DefaultCallerSupplier}.
//     * This method is mutually exclusive with {@link #setCallerClassBlacklist(Set)} and
//     * {@link #setMethodResolver(Supplier)}. If a caller black list is set, the predicate will just test if the
//     * declared class of the stack frame is not in the blacklist.
//     *
//     * @param stackFramePredicate The stack frame predicate.
//     * @return The RunaboutServiceBuilder.
//     */
//    public RunaboutServiceBuilder setStackFramePredicate(Predicate<StackWalker.StackFrame> stackFramePredicate) {
//        this.stackFramePredicate = Objects.requireNonNull(stackFramePredicate, "Predicate cannot be null.");
//        return this;
//    }

    // TODO
    public RunaboutServiceBuilder setListener(RunaboutListener listener) {
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
