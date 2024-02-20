package dev.runabout;

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
}
