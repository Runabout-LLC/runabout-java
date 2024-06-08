package dev.runabout;

/**
 * Listener for events in the Runabout library. Currently only used for errors.
 */
@FunctionalInterface
public interface RunaboutListener {

    /**
     * Called when an error occurs in the Runabout library.
     *
     * @param error The error that occurred.
     */
    void onError(final Throwable error);
}
