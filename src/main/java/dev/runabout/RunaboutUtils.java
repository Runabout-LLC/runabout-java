package dev.runabout;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class for Runabout.
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
}
