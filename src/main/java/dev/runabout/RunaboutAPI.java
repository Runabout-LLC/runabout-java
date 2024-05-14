package dev.runabout;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class RunaboutAPI {

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

    RunaboutAPI(final RunaboutAPIBuilder builder) {
        this(builder.getReadTimeout(), builder.getConnectTimeout(), builder.getMaxBodyLength(), builder.getMaxThreads(),
                builder.getIngestURL());
    }

    RunaboutAPI(int readTimeout, int connectTimeout, int maxBodyLength, int threadCount, String ingestURL) {
        this.ingestURI = toURI(ingestURL);
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.maxBodyLength = maxBodyLength;
        this.queueSize = 1000; // TODO
        this.eventQueue = new ArrayBlockingQueue<>(queueSize);
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
        final HttpRequest request = requestBuilder

                .POST(HttpRequest.BodyPublishers.ofString(contents)).build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(i -> { if (i < 200 || i >= 300) System.err.println("Failed to emit event: " + i); });
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
                final JsonObject request = new JsonObjectImpl.JsonFactoryImpl().get(); // TODO parameterize
                final List<JsonObject> events = new ArrayList<>();

                while (!eventQueue.isEmpty()) {
                    events.add(eventQueue.take());
                }

                if (!events.isEmpty()) {
                    request.put("scenarios", JsonObject.class, events);
                    final String contents = request.toJson();
                    emit(contents);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
