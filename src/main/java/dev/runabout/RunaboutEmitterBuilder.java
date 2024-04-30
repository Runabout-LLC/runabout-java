package dev.runabout;

public class RunaboutEmitterBuilder {

    private int    readTimeout;
    private int    connectTimeout;
    private int    maxBodyLength;
    private int    maxThreads;
    private String ingestURL;

    public int getReadTimeout() {
        return readTimeout;
    }

    public RunaboutEmitterBuilder setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public RunaboutEmitterBuilder setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getMaxBodyLength() {
        return maxBodyLength;
    }

    public RunaboutEmitterBuilder setMaxBodyLength(int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
        return this;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public RunaboutEmitterBuilder setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }

    public String getIngestURL() {
        return ingestURL;
    }

    public RunaboutEmitterBuilder setIngestURL(String ingestURL) {
        this.ingestURL = ingestURL;
        return this;
    }
}
