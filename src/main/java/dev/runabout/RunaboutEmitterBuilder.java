package dev.runabout;

public class RunaboutEmitterBuilder {

    private int    readTimeout;
    private int    connectTimeout;
    private int    maxBodyLength;
    private int    maxThreads;
    private String ingestURL;

    /**
     * Constructor sets default values.
     */
    public RunaboutEmitterBuilder() {
        readTimeout = 10000;
        connectTimeout = 30000;
        maxBodyLength = 50_000;
        maxThreads = 1;
        ingestURL = RunaboutConstants.INGEST_URL;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Set the read timeout used when connecting to the ingest URL.
     *
     * @param readTimeout integer timeout.
     * @return The RunaboutEmitterBuilder instance.
     */
    public RunaboutEmitterBuilder setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Set the connection timeout used when connecting to the ingest URL.
     *
     * @param connectTimeout integer timeout.
     * @return The RunaboutEmitterBuilder instance.
     */
    public RunaboutEmitterBuilder setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getMaxBodyLength() {
        return maxBodyLength;
    }

    /**
     * Set the maximum size of a request body in bytes.
     *
     * @param maxBodyLength integer maximum number of bytes.
     * @return The RunaboutEmitterBuilder instance.
     */
    public RunaboutEmitterBuilder setMaxBodyLength(int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
        return this;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * Set the maximum number of threads in the thread pool to consume and emit events.
     *
     * @param maxThreads integer total number of threads.
     * @return The RunaboutEmitterBuilder instance.
     */
    public RunaboutEmitterBuilder setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
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
    public RunaboutEmitterBuilder setIngestURL(String ingestURL) {
        this.ingestURL = ingestURL;
        return this;
    }
}
