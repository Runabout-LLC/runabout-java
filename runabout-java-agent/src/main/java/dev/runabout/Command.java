package dev.runabout;

import java.util.List;

class Command {

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
        return null; // TODO
    }
}
