package dev.runabout.agent;

import java.util.Objects;

public class Instruction {

    private static final String TIMEOUT_KEY = "timeout";
    private static final String REFERENCE_KEY = "reference";
    private static final String REFERENCE_TYPE_KEY = "referenceType";

    private final long timeout;
    private final String reference;
    private final ReferenceType referenceType;

    public Instruction(final long timeout, final String reference, final ReferenceType referenceType) {
        this.timeout = timeout;
        this.reference = reference;
        this.referenceType = referenceType;
    }

    public long getTimeout() {
        return timeout;
    }

    public String getReference() {
        return reference;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public String getReferenceURI() {
        return referenceType + ":" + reference;
    }

    public static Instruction of(final String json) {

        final String cleanJson = json.trim();
        if (json.charAt(0) != '{' || json.charAt(json.length() - 1) != '}') {
            throw new IllegalArgumentException("Invalid JSON: " + json);
        }

        final String body = cleanJson.substring(1, cleanJson.length() - 1);

        Long timeout = null;
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
                case REFERENCE_KEY:
                    reference = value;
                    break;
                case REFERENCE_TYPE_KEY:
                    referenceType = ReferenceType.valueOf(value.toUpperCase());
                    break;
            }
        }

        return new Instruction(Objects.requireNonNull(timeout, "Invalid command, missing timeout"),
                Objects.requireNonNull(reference, "Invalid command, missing reference"),
                Objects.requireNonNull(referenceType, "Invalid command, missing referenceType"));
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeout, reference, referenceType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Instruction other = (Instruction) obj;
        return timeout == other.timeout &&
                Objects.equals(reference, other.reference) &&
                referenceType == other.referenceType;
    }

    public enum ReferenceType {
        CLASS,
        METHOD
    }
}
