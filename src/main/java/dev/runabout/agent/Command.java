package dev.runabout.agent;

import dev.runabout.annotations.RunaboutEnabled;

import java.util.Objects;

// TODO change to instruction?
class Command {

    private static final String ACTION_KEY = "action";
    private static final String TIMEOUT_KEY = "timeout";
    private static final String REFERENCE_KEY = "reference";
    private static final String REFERENCE_TYPE_KEY = "referenceType";

    private final long timeout;
    private final Action action; // TODO do we want action? It will be good in push-based, but does it make sense now?
    private final String reference;
    private final ReferenceType referenceType;

    @RunaboutEnabled
    Command(final long timeout, final Action action, final String reference, final ReferenceType referenceType) {
        this.timeout = timeout;
        this.action = action;
        this.reference = reference;
        this.referenceType = referenceType;
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

    public ReferenceType getReferenceType() {
        return referenceType;
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
        ReferenceType referenceType = null;

        for (final String keyValuePair : body.split(",")) {

            final String[] keyValue = keyValuePair.split(":");

            if (keyValue.length != 2) {
                throw new IllegalArgumentException(String.format("Invalid JSON key value pair: %s", json));
            }

            final String key = keyValue[0].trim().replaceAll("\"", "");
            final String value = keyValue[1].trim().replaceAll("\"", "");

            switch (key) {
                case TIMEOUT_KEY:
                    timeout = Long.parseLong(value);
                    break;
                case ACTION_KEY:
                    action = Action.valueOf(value.toUpperCase());
                    break;
                case REFERENCE_KEY:
                    reference = value;
                    break;
                case REFERENCE_TYPE_KEY:
                    referenceType = ReferenceType.valueOf(value.toUpperCase());
                    break;
            }
        }

        return new Command(Objects.requireNonNull(timeout, "Invalid command, missing timeout"),
                Objects.requireNonNull(action, "Invalid command, missing action"),
                Objects.requireNonNull(reference, "Invalid command, missing reference"),
                Objects.requireNonNull(referenceType, "Invalid command, missing referenceType"));
    }

    public enum Action {
        ENABLE,
        DISABLE
    }

    public enum ReferenceType {
        CLASS,
        METHOD
    }
}
