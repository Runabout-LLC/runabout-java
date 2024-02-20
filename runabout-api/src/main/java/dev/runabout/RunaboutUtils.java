package dev.runabout;

public class RunaboutUtils {

    private RunaboutUtils() {
        // Static access only.
    }

    public static String escapeQuotesOneLayer(final String input) {
        return input
                .replaceAll("(?<!\\\\)\\\\\\\\\\\\\\\"","\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"") // go from 3 to 7 escapes
                .replaceAll("(?<!\\\\)\\\\\\\"", "\\\\\\\\\\\\\"") // go from 1 to 3 escapes
                .replaceAll("(?<!\\\\)\\\"", "\\\\\""); // go from 0 to 1 escape
    }
}
