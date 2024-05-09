package dev.runabout;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

class RunaboutEmitter {

    private final int readTimeout;
    private final int connectTimeout;
    private final int maxBodyLength;
    private final URI ingestURI;
    private final int queueSize;
    private final ExecutorService executorService;
    private final HttpClient httpClient;
    private final HttpRequest.Builder requestBuilder;
    private final Runnable failedToQueueCallback;
    private final BlockingQueue<String> eventQueue;

    RunaboutEmitter(final RunaboutEmitterBuilder builder) {
        this(builder.getReadTimeout(), builder.getConnectTimeout(), builder.getMaxBodyLength(), builder.getMaxThreads(),
                builder.getIngestURL());
    }

    RunaboutEmitter(int readTimeout, int connectTimeout, int maxBodyLength, int threadCount, String ingestURL) {
        this.ingestURI = toURI(ingestURL);
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.maxBodyLength = maxBodyLength;
        this.queueSize = 1000; // TODO
        this.eventQueue = new LinkedBlockingQueue<>(queueSize);
        executorService = Executors.newFixedThreadPool(threadCount);
        httpClient = HttpClient.newBuilder()
                .build();
        requestBuilder = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(ingestURI);
        failedToQueueCallback = () -> {};
    }

    public void queueEmission(final String contents) {
        if (!eventQueue.offer(contents)) {
            failedToQueueCallback.run();
        }
        executorService.execute(new Worker());
    }

    /**
     * Emit a json payload containing a scenario, event ID, and contextual data.
     *
     * @param contents String json contents.
     */
    void emit(final String contents) {
        final HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(contents)).build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    private static URI toURI(final String url) {
        try {
            return new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RunaboutException("Invalid runabout ingest URL", e);
        }
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            try {
                final StringBuilder stringBuilder = new StringBuilder();

                while (!eventQueue.isEmpty() && stringBuilder.length() < maxBodyLength) {
                    final String event = eventQueue.take();
                    stringBuilder.append(event);
                }

                if (stringBuilder.length() > 0) {
                    emit(stringBuilder.toString());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
