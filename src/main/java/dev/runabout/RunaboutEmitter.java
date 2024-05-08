package dev.runabout;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

class RunaboutEmitter {

    private final int             readTimeout;
    private final int             connectTimeout;
    private final int             maxBodyLength;
    private final String          ingestURL;
    private final ExecutorService executorService;

    private final BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();

    RunaboutEmitter(final RunaboutEmitterBuilder builder) {
        this(builder.getReadTimeout(), builder.getConnectTimeout(), builder.getMaxBodyLength(), builder.getMaxThreads(),
                builder.getIngestURL());
    }

    RunaboutEmitter(int readTimeout, int connectTimeout, int maxBodyLength, int threadCount, String ingestURL) {
        this.ingestURL = ingestURL;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
        this.maxBodyLength = maxBodyLength;
        executorService = Executors.newFixedThreadPool(threadCount);
    }

    public void queueEmission(final String contents) {
        eventQueue.add(contents);
        executorService.execute(new Worker());
    }

    /**
     * Emit a json payload containing a scenario, event ID, and contextual data.
     *
     * @param contents String json contents.
     */
    void emit(final String contents) throws IOException {

        final URL endpoint = new URL(ingestURL);
        final URLConnection conn = endpoint.openConnection();

        conn.setReadTimeout(readTimeout);
        conn.setConnectTimeout(connectTimeout);
        conn.setDoOutput(false);
        conn.setRequestProperty("Content-Type", "application/json;charset=utf8");

        try (OutputStream out = conn.getOutputStream()) {
            out.write(contents.getBytes(StandardCharsets.UTF_8));
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
            } catch (Exception e) {
                // Do nothing at this point? TODO
            }
        }
    }
}
