package dev.runabout.agent;

import java.util.Objects;

class Command {

    private static final String TIMEOUT_KEY = "timeout";
    private static final String REFERENCE_KEY = "reference";

    private final long timeout;
    private final String reference;

    Command(long timeout, String reference) {
        this.timeout = timeout;
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public long getTimeout() {
        return timeout;
    }

    public static Command of(final String json) {

        final String cleanJson = json.trim();
        if (json.charAt(0) != '{' || json.charAt(json.length() - 1) != '}') {
            throw new IllegalArgumentException("Invalid JSON: " + json);
        }

        final String body = cleanJson.substring(1, cleanJson.length() - 1);

        Long timeout = null;
        String reference = null;

        for (final String keyValuePair : body.split(",")) {

            final String[] keyValue = keyValuePair.split(":");

            if (keyValue.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid JSON key value pair: %s", json));
            }

            final String key = keyValue[0].trim().replaceAll("\"", "");
            final String value = keyValue[1].trim().replaceAll("\"", "");

            if (key.equals(TIMEOUT_KEY)) {
                timeout = Long.parseLong(value);
            } else if (key.equals(REFERENCE_KEY)) {
                reference = value;
            }
        }

        return new Command(Objects.requireNonNull(timeout, "Invalid command, missing timeout"),
                Objects.requireNonNull(reference, "Invalid command, missing reference"));
    }
}
