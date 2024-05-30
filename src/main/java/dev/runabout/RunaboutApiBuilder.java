package dev.runabout;

import java.util.function.Supplier;

public class RunaboutApiBuilder {

    private int    threads;
    private int    queueSize;
    private long   timeout;
    private String ingestURL;

    private final Supplier<String> apiTokenSupplier;

    /**
     * Constructor for RunaboutAPIBuilder. TODO
     * @param apiTokenSupplier
     */
    public RunaboutApiBuilder(final Supplier<String> apiTokenSupplier) {
        threads = 1;
        queueSize = 1_000;
        timeout = 30_000;
        ingestURL = RunaboutConstants.INGEST_URL;
        this.apiTokenSupplier = apiTokenSupplier;
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * Set the connection timeout used when connecting to the ingest URL.
     *
     * @param timeout long timeout in milliseconds.
     * @return The RunaboutEmitterBuilder instance.
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
     * @return The RunaboutEmitterBuilder instance.
     */
    public RunaboutApiBuilder setThreads(int threads) {
        this.threads = threads;
        return this;
    }

    public String getIngestURL() {
        return ingestURL;
    }

    /**
     * Sets the ingest-url to post events to.
     *
     * @param ingestURL String url to make requests to.
     * @return The RunaboutEmitterBuilder instance.
     */
    public RunaboutApiBuilder setIngestURL(String ingestURL) {
        this.ingestURL = ingestURL;
        return this;
    }

    public Supplier<String> getApiTokenSupplier() {
        return apiTokenSupplier;
    }

    public RunaboutApi build() {
        return new RunaboutApiImpl(this);
    }
}
