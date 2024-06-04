package dev.runabout;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * A builder for creating a RunaboutAPI.
 * The default implementation will
 */
public class RunaboutApiBuilder {

    private URI uri;
    private long timeout;
    private Executor executor;
    private RunaboutListener listener;
    private Queue<RunaboutScenario> queue;

    private final Supplier<String> tokenSupplier;

    /**
     * Constructor for RunaboutAPIBuilder. The only required parameter is the apiTokenSupplier, which
     * should supply an organization's API token. To get an API token, visit
     * <a href="https://runabout.dev">runabout.dev</a>.
     *
     * @param tokenSupplier Supplier of an organization's API token.
     */
    public RunaboutApiBuilder(final Supplier<String> tokenSupplier) {
        uri = URI.create(RunaboutConstants.INGEST_SCENARIOS_URL);
        timeout = 30_000;
        queue = new ArrayBlockingQueue<>(1000);
        this.tokenSupplier = tokenSupplier;
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

    public Executor getExecutor() {
        return executor;
    }

    /**
     * Set the executor used to make requests. Default is a fixed thread pool with a single thread.
     *
     * @param executor Executor service to use.
     * @return The RunaboutApiBuilder instance.
     */
    public RunaboutApiBuilder setExecutor(Executor executor) {
        this.executor = Objects.requireNonNull(executor, "Executor cannot be null");
        return this;
    }

    public Queue<RunaboutScenario> getQueue() {
        return queue;
    }

    /**
     * Set the queue implementation used to hand off scenarios to the executor service, which will make the requests.
     *
     * @param queue Queue implementation to use.
     * @return The RunaboutApiBuilder instance.
     */
    public RunaboutApiBuilder setQueue(Queue<RunaboutScenario> queue) {
        this.queue = Objects.requireNonNull(queue, "Queue size cannot be null");
        return this;
    }

    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI to post scenarios to.
     *
     * @param uri URI to make requests to.
     * @return The RunaboutApiBuilder instance.
     */
    public RunaboutApiBuilder setUri(URI uri) {
        this.uri = uri;
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

    public Supplier<String> getTokenSupplier() {
        return tokenSupplier;
    }

    public RunaboutApi build() {
        this.executor = Optional.ofNullable(this.executor).orElseGet(() -> Executors.newFixedThreadPool(1));
        return new RunaboutApiImpl(this);
    }
}
