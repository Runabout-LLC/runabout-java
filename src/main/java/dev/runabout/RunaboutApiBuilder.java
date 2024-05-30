package dev.runabout;

import java.util.function.Supplier;

/**
 * A builder for creating a RunaboutAPI.
 * The default implementation will
 */
public class RunaboutApiBuilder {

    private int    threads;
    private int    queueSize;
    private long   timeout;
    private String url;
    private RunaboutListener listener;

    private final Supplier<String> apiTokenSupplier;

    /**
     * Constructor for RunaboutAPIBuilder. TODO
     * @param apiTokenSupplier
     */
    public RunaboutApiBuilder(final Supplier<String> apiTokenSupplier) {
        threads = 1;
        queueSize = 1_000;
        timeout = 30_000;
        url = RunaboutConstants.INGEST_SCENARIOS_URL;
        this.apiTokenSupplier = apiTokenSupplier;
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * Set the connection timeout used when connecting to the ingest URL.
     *
     * @param timeout long timeout in milliseconds.
     * @return The RunaboutApiBuilder instance.
     */
    public RunaboutApiBuilder setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getQueueSize() {
        return queueSize;
    }

    /**
     * TODO
     * @param queueSize
     * @return
     */
    public RunaboutApiBuilder setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    /**
     * Set the maximum number of threads in the thread pool to consume and emit events.
     *
     * @param threads integer total number of threads.
     * @return The RunaboutApiBuilder instance.
     */
    public RunaboutApiBuilder setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Sets the url to post events to.
     *
     * @param url String url to make requests to.
     * @return The RunaboutApiBuilder instance.
     */
    public RunaboutApiBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public RunaboutListener getListener() {
        return listener;
    }


    /**
     * Set a listener to get a callback about errors.
     * @param listener RunaboutListener instance.
     * @return The RunaboutApiBuilder instance.
     */
    public RunaboutApiBuilder setListener(RunaboutListener listener) {
        this.listener = listener;
        return this;
    }

    public Supplier<String> getApiTokenSupplier() {
        return apiTokenSupplier;
    }

    public RunaboutApi build() {
        return new RunaboutApiImpl(this);
    }
}
