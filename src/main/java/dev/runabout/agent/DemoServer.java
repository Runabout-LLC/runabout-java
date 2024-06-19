package dev.runabout.agent;

import com.sun.net.httpserver.HttpServer;
import dev.runabout.RunaboutService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class DemoServer {

    public static void main(String[] args) throws IOException {

        final RunaboutService service = RunaboutService.getService("test", "test");
        final RunaboutAgent agent = new RunaboutAgentImpl(null, service);
        agent.install();

        final HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress("localhost", 8080), 0);
        server.setExecutor(null);
        server.start();

        server.createContext("/install", exchange -> {
            try {

            final byte[] bytes = exchange.getRequestBody().readAllBytes();
            final String body = new String(bytes);
            final Instruction instruction = Instruction.of(body);
            agent.refresh(Set.of(instruction));
            exchange.sendResponseHeaders(200, 0);
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, e.getMessage().length());
                exchange.getResponseBody().write(e.getMessage().getBytes());
            }
            exchange.close();
        });

        server.createContext("/disable", exchange -> {
            agent.refresh(new HashSet<>());
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });

        server.createContext("/print", exchange -> {
            final byte[] bytes = exchange.getRequestBody().readAllBytes();
            final String body = new String(bytes);
            print(body);
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
        });
    }

    private static void print(String body) {
        System.out.println(body);
    }
}
