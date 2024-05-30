package dev.runabout;

import dev.runabout.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

class RunaboutApiImpl implements RunaboutApi {

    @Nullable
    private final RunaboutListener listener;

    private final long timeout;
    private final HttpClient httpClient;
    private final Supplier<String> apiTokenSupplier;
    private final ExecutorService executorService;
    private final HttpRequest.Builder requestBuilder;
    private final BlockingQueue<RunaboutScenario> scenarioQueue;

    RunaboutApiImpl(final RunaboutApiBuilder builder) {
        this.listener = builder.getListener();
        this.timeout = builder.getTimeout();
        this.apiTokenSupplier = builder.getApiTokenSupplier();
        this.executorService = Executors.newFixedThreadPool(builder.getThreads());
        this.scenarioQueue = new ArrayBlockingQueue<>(builder.getQueueSize());
        this.httpClient = HttpClient.newBuilder().build();
        final URI ingestURI = toURI(builder.getUrl());
        this.requestBuilder = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(ingestURI);
    }

    public void ingestScenario(final RunaboutScenario scenario) {
        Objects.requireNonNull(scenario.getMethod(), "Scenario cannot be null");
        if (!scenarioQueue.offer(scenario)) {
            onError(new RunaboutException("Runabout scenario queue is full"));
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
                .POST(HttpRequest.BodyPublishers.ofString(contents))
                .build();
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .orTimeout(timeout, java.util.concurrent.TimeUnit.MILLISECONDS)
                .thenApply(HttpResponse::statusCode)
                .thenAccept(code -> {
                    if (code < 200 || code >= 300) {
                        onError(new RunaboutException("Runabout API error. Error code: " + code));
                    }
                });

        //
        // Clear out the authorization header after the request is sent for security purposes.
        //
        requestBuilder.setHeader("Authorization", null);
    }

    private void onError(final Throwable t) {
        Optional.ofNullable(listener).ifPresent(l -> l.onError(t));
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
                final RunaboutScenario scenario = scenarioQueue.poll(timeout, TimeUnit.MILLISECONDS);
                if (scenario != null) {
                    final JsonObject request = new JsonObjectImpl();
                    final JsonObject scenarioJson = scenario.toJsonObject();
                    request.put(RunaboutConstants.SCENARIOS_KEY, JsonObject.class, List.of(scenarioJson));
                    ingestScenario(request.toJson());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
