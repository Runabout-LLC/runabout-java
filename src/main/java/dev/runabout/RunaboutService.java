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
    static RunaboutService<JsonObject> getService() {
        return RunaboutServiceBuilder.getDefaultBuilder().build();
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
     * See {@link #toRunaboutJson(Method, Object...)} for more info.
     *
     * @param objects The objects to convert to Runabout inputs in JSON.
     * @return A JSON object.
     */
    default T toRunaboutJson(final Object... objects) {
        return toRunaboutJson(getCallerMethod(), objects);
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
    T toRunaboutJson(final Method method, final Object... objects);

    /**
     * Converts a set of objects to a Runabout JSON string. See {@link #toRunaboutJson(Object...)} for more info.
     * @param objects The objects to convert to Runabout inputs in JSON.
     * @return A JSON object as a String.
     */
    default String toRunaboutString(final Object... objects) {
        return toRunaboutJson(objects).toJson();
    }

    /**
     * Converts a Method and set of objects to a Runabout JSON string.
     * See {@link #toRunaboutJson(Object...)} for more info.
     * @param method The method that the objects are arguments for.
     * @param objects The objects to convert to Runabout inputs in JSON.
     * @return A JSON object as a String.
     */
    default String toRunaboutString(final Method method, final Object... objects) {
        return toRunaboutJson(method, objects).toJson();
    }
}
