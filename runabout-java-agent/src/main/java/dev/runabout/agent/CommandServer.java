package dev.runabout.agent;

import com.sun.net.httpserver.HttpServer;
import dev.runabout.RunaboutListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

class CommandServer {

    private final int port;
    private final RunaboutListener listener;
    private final Consumer<Command> commandConsumer;

    CommandServer(final int port, final RunaboutListener listener, Consumer<Command> commandConsumer) {
        this.port = port;
        this.listener = listener;
        this.commandConsumer = commandConsumer;
    }

    public void start() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.setExecutor(Executors.newFixedThreadPool(2));
            server.start();
            server.createContext("/command", exchange -> {
                try {
                    final String json = new String(exchange.getRequestBody().readAllBytes());
                    final Command command = Command.of(json);
                    commandConsumer.accept(command);
                    exchange.sendResponseHeaders(200, 0);
                } catch (Exception e) {
//                    listener.onError(e); TODO does this make sense here?
                    exchange.sendResponseHeaders(500, e.getMessage().length());
                    final OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(e.getMessage().getBytes());
                    outputStream.close();
                } finally {
                    exchange.close();
                }
            });
        } catch (IOException e) {
            listener.onError(e);
        }
    }
}
