package dev.runabout;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * TODO TEMPORARY FOR CROSS JAR DEVELOPMENT
 * TODO DELETE ME
 */
public class RunaboutUtils {

    private RunaboutUtils() {
        // Static access only.
    }

    /**
     * Escapes quotes in a string 3 layers deep.
     *
     * @param input The input string.
     * @return The input string with quotes escaped.
     */
    public static String escapeQuotesOneLayer(final String input) {
        return input
                // go from 3 to 7 escapes
                .replaceAll("(?<!\\\\)\\\\\\\\\\\\\\\"","\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"")
                // go from 1 to 3 escapes
                .replaceAll("(?<!\\\\)\\\\\\\"", "\\\\\\\\\\\\\"")
                // go from 0 to 1 escape
                .replaceAll("(?<!\\\\)\\\"", "\\\\\"");
    }

    /**
     * Default implementation for converting a {@link Method} to a Runabout-readable String.
     *
     * @param method The method to create a string referencing.
     * @return A string in a format known to Runabout.
     */
    public static String methodToRunaboutString(final Method method) {
        return method.getDeclaringClass().getCanonicalName() + "#" + method.getName() + "(" +
                 Arrays.stream(method.getParameterTypes())
                         .map(Class::getCanonicalName)
                         .collect(Collectors.joining(", ")) + ")";
    }

    public static Method runaboutStringToMethod(final String string) {
        final String className = string.substring(0, string.indexOf("#"));

        final Class<?> clazz = getClass(className);
        final String methodName = string.substring(string.indexOf("#") + 1, string.indexOf("("));
        final String parameterTypesString = string.substring(string.indexOf("(") + 1, string.indexOf(")"));
        final Class<?>[] parameterTypes = Arrays.stream(parameterTypesString.split(", "))
                .map(RunaboutUtils::getClass)
                .toArray(Class[]::new);

        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (final NoSuchMethodException e) {
            throw new RunaboutException(String.format("Could not find method: %1$s on class: %2$s", methodName, className));
        }
    }

    public static Class<?> getClass(final String className) {
        try {
            return Class.forName(className);
        } catch (final ClassNotFoundException e) {
            throw new RunaboutException("Could not find class: " + className);
        }
    }
}
