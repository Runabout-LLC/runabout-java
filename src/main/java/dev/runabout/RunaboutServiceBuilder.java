package dev.runabout;

import dev.runabout.annotations.ToRunabout;
import dev.runabout.utils.RunaboutUtils;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A builder for creating a RunaboutService.
 */
public class RunaboutServiceBuilder {

    private boolean excludeSuper;
    private Supplier<Method> callerSupplier;
    private Set<Class<?>> callerClassBlacklist;
    private RunaboutSerializer customSerializer;
    private Consumer<Throwable> throwableConsumer;
    private Supplier<Timestamp> datetimeSupplier;
    private Function<Method, String> methodToStringFunction;
    private Predicate<StackWalker.StackFrame> stackFramePredicate;
    private Supplier<JsonObject> jsonFactory;
    private RunaboutListener listener;

    private final String projectName;
    private final RunaboutAPIConfig apiConfig;

    /**
     * Creates a new RunaboutServiceBuilder with the given JSON object factory.
     * The supplier should create new instances of JSON objects.
     *
     * @param projectName The name of the project to log scenario under.
     * @param apiConfig The configuration for the Runabout API.
     */
    public RunaboutServiceBuilder(String projectName, RunaboutAPIConfig apiConfig) {
        this.projectName = projectName;
        this.apiConfig = apiConfig;
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
     * By default, the {@link RunaboutService} will use {@link DefaultCallerSupplier}.
     *
     * @param callerSupplier A supplier that returns the desired {@link Method} to include in the Runabout JSON.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder setCallerSupplier(Supplier<Method> callerSupplier) {
        this.callerSupplier = Objects.requireNonNull(callerSupplier, "Caller supplier cannot be null.");
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
    public RunaboutServiceBuilder setCallerClassBlacklist(Set<Class<?>> callerClassBlacklist) {
        this.callerClassBlacklist = Objects.requireNonNull(callerClassBlacklist,
                "Caller class blacklist cannot be null.");
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
    public RunaboutServiceBuilder setCustomSerializer(RunaboutSerializer customSerializer) {
        this.customSerializer = Objects.requireNonNull(customSerializer, "Custom serializer cannot be null.");
        return this;
    }

    /**
     * Sets a consumer for any throwables that occur during the RunaboutService's operation.
     * By default, the service will not throw exceptions, but will throw errors.
     *
     * @param throwableConsumer a consumer
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder setThrowableConsumer(Consumer<Throwable> throwableConsumer) {
        this.throwableConsumer = Objects.requireNonNull(throwableConsumer, "Throwable consumer cannot be null.");
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
    public RunaboutServiceBuilder setExcludeSuper(boolean excludeSuper) {
        this.excludeSuper = excludeSuper;
        return this;
    }

    /**
     * Sets the datetime supplier for the RunaboutService.
     * The supplier should return the current datetime as an {@link Timestamp}.
     * By default, the supplier will return the current datetime using {@link Instant#now()}.
     *
     * @param datetimeSupplier The datetime supplier.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder setDatetimeSupplier(Supplier<Timestamp> datetimeSupplier) {
        this.datetimeSupplier = datetimeSupplier;
        return this;
    }

    /**
     * Sets the methodToStringFunction used to convert the caller method to a string for the JSON object
     * in a way that Runabout knows how to read. The expected format is as follows:
     * Fully qualified classname + "#" + method name + "(" + fully qualified argument types in order
     * delimited by ", " ... + ")"
     *
     * @param methodToStringFunction The methodToString function.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder setMethodToStringFunction(Function<Method, String> methodToStringFunction) {
        this.methodToStringFunction = methodToStringFunction;
        return this;
    }

    /**
     * Sets the stack frame predicate for the RunaboutService.
     * The predicate is used to filter out stack frames when determining the caller in {@link DefaultCallerSupplier}.
     * This method is mutually exclusive with {@link #setCallerClassBlacklist(Set)} and
     * {@link #setCallerSupplier(Supplier)}. If a caller black list is set, the predicate will just test if the
     * declared class of the stack frame is not in the blacklist.
     *
     * @param stackFramePredicate The stack frame predicate.
     * @return The RunaboutServiceBuilder.
     */
    public RunaboutServiceBuilder setStackFramePredicate(Predicate<StackWalker.StackFrame> stackFramePredicate) {
        this.stackFramePredicate = Objects.requireNonNull(stackFramePredicate, "Predicate cannot be null.");
        return this;
    }

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

        if (callerClassBlacklist != null && stackFramePredicate != null) {
            throw new RunaboutException("Caller class blacklist and stack frame predicate setters " +
                    "are mutually exclusive.");
        }

        final Predicate<StackWalker.StackFrame> defaultCallerSupplierPredicate = Optional.ofNullable(stackFramePredicate)
                .orElseGet(() -> Optional.ofNullable(callerClassBlacklist)
                        .map(DefaultCallerSupplier::getCallerClassPredicate)
                        .orElse(null));

        if (callerSupplier != null && defaultCallerSupplierPredicate != null) {
            throw new RunaboutException("Caller supplier and caller predicate/blacklist " +
                    "setters are mutually exclusive.");
        }

        final Supplier<Method> callerSupplierFinal = Optional.ofNullable(callerSupplier)
                .orElseGet(() -> {
                    final Predicate<StackWalker.StackFrame> predicateFinal = Optional
                            .ofNullable(defaultCallerSupplierPredicate).orElse(stackFrame -> true);
                    return new DefaultCallerSupplier(predicateFinal);
                });

        final RunaboutSerializer customSerializerFinal = Optional.ofNullable(this.customSerializer)
                .orElseGet(RunaboutSerializer::getSerializer);

        final Consumer<Throwable> throwableConsumerFinal = Optional.ofNullable(throwableConsumer)
                .orElse(RunaboutServiceBuilder::defaultThrowableConsumer);

        final Supplier<Timestamp> datetimeSupplierFinal = Optional.ofNullable(this.datetimeSupplier)
                .orElse(() -> Timestamp.from(Instant.now()));

        final Function<Method, String> methodToStringFunctionFinal = Optional.ofNullable(this.methodToStringFunction)
                .orElse(RunaboutUtils::methodToRunaboutString);

        final RunaboutAPI api = new RunaboutAPI(apiConfig, listener);

        return new RunaboutServiceImpl(projectName, excludeSuper, throwableConsumerFinal, callerSupplierFinal,
                customSerializerFinal, jsonFactory, datetimeSupplierFinal, methodToStringFunctionFinal, api);
    }

    private static void defaultThrowableConsumer(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        }
    }
}
