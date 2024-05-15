package dev.runabout;

// TODO
public interface RunaboutListener {

    void onError(final Throwable error);

    void onAPIError(final int code, final String message);
}
