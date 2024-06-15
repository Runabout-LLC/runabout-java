package dev.runabout.agent;

import dev.runabout.annotations.RunaboutEnabled;
import dev.runabout.annotations.RunaboutParameter;

import java.util.Objects;

// TODO make not public
public class Command {

    private static final String ACTION_KEY = "action";
    private static final String TIMEOUT_KEY = "timeout";
    private static final String REFERENCE_KEY = "reference";

    private final long timeout;
    private final Action action;
    private final String reference;

    @RunaboutEnabled
    public Command(@RunaboutParameter("timeout") long timeout,
                   @RunaboutParameter("action") Action action,
                   @RunaboutParameter("reference") String reference) {
        this.timeout = timeout;
        this.action = action;
        this.reference = reference;
    }

    public long getTimeout() {
        return timeout;
    }

    public Action getAction() {
        return action;
    }

    public String getReference() {
        return reference;
    }

    public static Command of(final String json) {

        final String cleanJson = json.trim();
        if (json.charAt(0) != '{' || json.charAt(json.length() - 1) != '}') {
            throw new IllegalArgumentException("Invalid JSON: " + json);
        }

        final String body = cleanJson.substring(1, cleanJson.length() - 1);

        Long timeout = null;
        Action action = null;
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
            } else if (key.equals(ACTION_KEY)) {
                action = Action.valueOf(value.toUpperCase());
            }
        }

        return new Command(Objects.requireNonNull(timeout, "Invalid command, missing timeout"),
                Objects.requireNonNull(action, "Invalid command, missing action"),
                Objects.requireNonNull(reference, "Invalid command, missing reference"));
    }

    public enum Action {
        ENABLE,
        DISABLE
    }
}
