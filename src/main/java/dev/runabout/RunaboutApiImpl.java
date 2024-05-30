package dev.runabout;

import dev.runabout.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

class RunaboutApiImpl implements RunaboutApi {
    private final long timeout;
    private final URI ingestURI;
    private final Supplier<String> apiTokenSupplier;
    private final ExecutorService executorService;
    private final HttpClient httpClient;
    private final HttpRequest.Builder requestBuilder;
    private final RunaboutListener listener;
    private final BlockingQueue<JsonObject> eventQueue;

    RunaboutApiImpl(final RunaboutApiBuilder builder) {
        this.apiTokenSupplier = builder.getApiTokenSupplier();
        this.ingestURI = toURI(builder.getIngestURL());
        this.timeout = builder.getTimeout();
        this.eventQueue = new ArrayBlockingQueue<>(builder.getQueueSize());
        this.executorService = Executors.newFixedThreadPool(builder.getThreads());
        this.httpClient = HttpClient.newBuilder()
                .build();
        this.requestBuilder = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(ingestURI);
        this.listener = null; // TODO
    }

    public void ingestScenario(final JsonObject object) {
        if (!eventQueue.offer(object)) {
            listener.onError(new RunaboutException("Event queue is full"));
        }
        executorService.execute(new Worker());
    }

    /**
     * Emit a json payload containing a scenario, event ID, and contextual data.
     *
     * @param contents String json contents.
     */
    private void ingestScenario(final String contents) {
        final HttpRequest request = requestBuilder
                .setHeader("Authorization", "Bearer " + apiTokenSupplier.get())
                .POST(HttpRequest.BodyPublishers.ofString(contents)).build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .orTimeout(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
                .thenApply(HttpResponse::statusCode)
                .thenAccept(code -> {
                    if (code < 200 || code >= 300) {
                        Optional.ofNullable(listener)
                                .ifPresent(l -> l.onError(new RunaboutException("API error: " + code)));// TODO
                    }
                });

        //
        // Clear out the authorization header after the request is sent for security purposes.
        //
        requestBuilder.setHeader("Authorization", null);
    }

    private static URI toURI(final String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RunaboutException("Invalid runabout ingest URL", e);
        }
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            try {
                final JsonObject event = eventQueue.poll(timeout, TimeUnit.MILLISECONDS);
                if (event != null) {
                    final JsonObject request = new JsonObjectImpl();
                    request.put(RunaboutConstants.SCENARIOS_KEY, JsonObject.class, List.of(event));
                    ingestScenario(request.toJson());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
