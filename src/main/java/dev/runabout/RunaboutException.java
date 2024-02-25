package dev.runabout;

public class RunaboutException extends RuntimeException {

    public RunaboutException(String message) {
        super(message);
    }

    public RunaboutException(String message, Throwable cause) {
        super(message, cause);
    }
}
