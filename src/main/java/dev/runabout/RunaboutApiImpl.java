package dev.runabout;

import dev.runabout.agent.Instruction;
import dev.runabout.annotations.Nullable;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

class RunaboutApiImpl implements RunaboutApi {

    @Nullable
    private final RunaboutListener listener;

    private final long timeout;
    private final HttpClient httpClient;
    private final Supplier<String> tokenSupplier;
    private final Executor executor;
    private final HttpRequest.Builder requestBuilder;
    private final Queue<RunaboutScenario> queue;

    RunaboutApiImpl(final RunaboutApiBuilder builder) {
        this.listener = builder.getListener();
        this.timeout = builder.getTimeout();
        this.tokenSupplier = builder.getTokenSupplier();
        this.executor = builder.getExecutor();
        this.queue = builder.getQueue();
        this.httpClient = HttpClient.newBuilder().build();
        this.requestBuilder = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .uri(builder.getUri());
    }

    @Override
    public void ingestScenario(final RunaboutScenario scenario) {
        Objects.requireNonNull(scenario.getMethod(), "Scenario cannot be null");
        if (!queue.offer(scenario)) {
            onError(new RunaboutException("Runabout scenario queue is full"));
        }
        executor.execute(new Worker());
    }

    @Override
    public List<Instruction> getLatestInstructions(String project) {
        return List.of(); // TODO
    }

    /**
     * Emit a json payload containing a scenario, event ID, and contextual data.
     *
     * @param contents String json contents.
     */
    private void ingestScenario(final String contents) {
        final HttpRequest request = requestBuilder
                .setHeader("Authorization", "Bearer " + tokenSupplier.get())
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
        requestBuilder.setHeader("Authorization", "");
    }

    private void onError(final Throwable t) {
        Optional.ofNullable(listener).ifPresent(l -> l.onError(t));
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            final RunaboutScenario scenario = queue.poll();
            if (scenario != null) {
                final JsonObject request = new JsonObjectImpl();
                final JsonObject scenarioJson = scenario.toJsonObject();
                request.put(RunaboutConstants.SCENARIOS_KEY, JsonObject.class, List.of(scenarioJson));
                ingestScenario(request.toJson());
            }
        }
    }
}
