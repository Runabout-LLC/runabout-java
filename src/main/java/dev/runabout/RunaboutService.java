package dev.runabout;

import java.lang.reflect.Method;

/**
 * Runabout interface for converting runtime java objects to JSON outputs that can be
 * used for replay debugging in <a href="https://www.runabout.dev">Runabout</a>.
 * The service has a generic type parameter that allows for the user to specify the type of JSON object to be used.
 * By default, the service uses the built-in lightweight {@link JsonObject} interface. To use a different type, use
 * {@link RunaboutServiceBuilder} to create a custom service.
 *
 * @param <T> The type of JSON object to use.
 */
public interface RunaboutService<T extends JsonObject> {

    /**
     * Gets the default RunaboutService which uses the built-in {@link JsonObject} type.
     *
     * @return A default RunaboutService.
     */
    static RunaboutService<JsonObject> getService(final String projectName) {
        return RunaboutServiceBuilder.getDefaultBuilder(projectName).build();
    }

    /**
     * Emit a scenario with eventId and contextual data to the runabout ingest API.
     * This method is intended to be non-blocking and implementations should enqueue the data
     * to be sent on another thread.
     *
     * @param eventId    Nullable String eventId for tracking scenarios that occurred in the same request.
     * @param properties Nullable JsonObject contextual data for adding additional info to scenarios.
     * @param scenario   NonNull JsonObject scenario.
     */
    void emitScenario(final String eventId, final T properties, final T scenario);

    /**
     * Create a scenario from an array of Objects and emit it.
     * See {@link RunaboutService#emitScenario(String, JsonObject, JsonObject)} for more info.
     *
     * @param eventId    Nullable String eventId for tracking scenarios that occurred in the same request.
     * @param properties Nullable JsonObject contextual data for adding additional info to scenarios.
     * @param objects    NonNull array (...) of Objects which may include "this" and method parameters.
     */
    default void emitScenario(final String eventId, final T properties, final Object... objects) {
        final T scenario = toScenario(objects);
        emitScenario(eventId, properties, scenario);
    }

    /**
     * Converts an object to a RunaboutInput.
     *
     * @param object The object to serialize.
     * @return A RunaboutInput representing the serialized object.
     */
    RunaboutInput serialize(final Object object);

    /**
     * Finds the closest caller method the stack that does not come from the Runabout library.
     *
     * @return The caller method.
     */
    Method getCallerMethod();

    /**
     * Converts a set of objects to a Runabout JSON object. Info about the caller method is implicitly determined.
     * See {@link #toScenario(Method, Object...)} for more info.
     *
     * @param objects The objects to convert to Runabout inputs in JSON.
     * @return A JSON object.
     */
    default T toScenario(final Object... objects) {
        return toScenario(getCallerMethod(), objects);
    }

    /**
     * Converts a method and the given objects to a Runabout JSON object.
     * This JSON format is known by Runabout and can be parsed by the Runabout web application and IDE plugin.
     * The format is as follows:
     * <code>
     * {
     * <br>    "version": "0.0.0", // The version of the Runabout JSON format.
     * <br>    "caller": "com.example.ClassName.methodName", // The caller method, from which the objects are arguments.
     * <br>     "inputs": [] // The runabout inputs (as JSON) from the objects passed in.
     * <br>}
     * </code>
     * <br>
     * The format of the inputs is as follows:
     * <code>
     * {
     * <br>    "type": "com.example.Value", // The fully qualified class of the input.
     * <br>    "value": "new Value()" // A String which is a Java expression, which evaluates to an object.
     * <br>    "dependencies": [] // The fully qualified class names for all dependencies of the input.
     * <br>}
     * </code>
     * @param method The method that the objects are arguments for.
     * @param objects The objects to convert to Runabout inputs in JSON.
     * @return A JSON object.
     */
    T toScenario(final Method method, final Object... objects);
}
