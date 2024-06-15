package dev.runabout;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
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
            server.start();
            server.setExecutor(null);
            server.createContext("/command", exchange -> {
                final String json = new String(exchange.getRequestBody().readAllBytes());
                try {
                    final Command command = Command.of(json);
                    commandConsumer.accept(command);
                    exchange.sendResponseHeaders(200, 0);
                } catch (Exception e) {
                    listener.onError(e);
                    exchange.sendResponseHeaders(500, 0);
                }
                exchange.close();
            });
        } catch (IOException e) {
            listener.onError(e);
        }
    }
}
