package dev.runabout;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class RunaboutApiTests {

    private static final String TEST_TOKEN = "test-token";

    @Test
    void basicScenarioTest() {
        final AtomicReference<String> token = new AtomicReference<>();
        final AtomicReference<Document> body = new AtomicReference<>();
        final AtomicBoolean failed = new AtomicBoolean(false);
        withLocalServer(
            (headers, s) -> {
                System.out.println("headers: " + headers);
                token.set(headers.get("Authorization").get(0).replace("Bearer ", ""));
                body.set(Document.parse(s));
                return 200;
            },
            port -> {
                final String url = "http://localhost:" + port + "/" + RunaboutConstants.SCENARIOS_KEY;
                final RunaboutApi api = new RunaboutApiBuilder(() -> TEST_TOKEN)
                        .setUri(URI.create(url))
                        .setListener(error -> failed.set(true))
                        .build();
                final RunaboutScenario scenario = new RunaboutScenario("method", "event00", "dev",
                        Timestamp.from(Instant.now()), new JsonObjectImpl().put("key", "value"), List.of(
                        new RunaboutInstance("type", "eval", Set.of("dep1", "dep2"))));
                api.ingestScenario(scenario);
                Awaitility.await().atMost(Duration.ofSeconds(10))
                        .pollInterval(Duration.ofMillis(100))
                        .until(() -> body.get() != null);
                Assertions.assertEquals(TEST_TOKEN, token.get());
                Assertions.assertEquals(new Document(RunaboutConstants.SCENARIOS_KEY, List.of(Document.parse(scenario.toJsonObject().toJson()))), body.get());
                Assertions.assertFalse(failed.get());
            });
    }

    @Test
    void testFailedRequest() {
        final AtomicBoolean failed = new AtomicBoolean(false);
        withLocalServer((headers, s) -> 401, port -> {
            final String url = "http://localhost:" + port + "/" + RunaboutConstants.SCENARIOS_KEY;
            final RunaboutApi api = new RunaboutApiBuilder(() -> TEST_TOKEN)
                    .setUri(URI.create(url))
                    .setListener(error -> failed.set(true))
                    .build();
            final RunaboutScenario scenario = new RunaboutScenario("method", "event00", "dev",
                    Timestamp.from(Instant.now()), new JsonObjectImpl().put("key", "value"), List.of(
                    new RunaboutInstance("type", "eval", Set.of("dep1", "dep2"))));
            api.ingestScenario(scenario);
            Awaitility.await().atMost(Duration.ofSeconds(10))
                    .pollInterval(Duration.ofMillis(100))
                    .until(failed::get);
        });
    }


    private static void withLocalServer(BiFunction<Headers,String,Integer> logic, Consumer<Integer> test) {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8085), 0);
            server.createContext("/" + RunaboutConstants.SCENARIOS_KEY, httpExchange -> {
                final Headers headers = httpExchange.getRequestHeaders();
                final String body = new String(httpExchange.getRequestBody().readAllBytes());
                final int responseCode = logic.apply(headers, body);
                httpExchange.sendResponseHeaders(responseCode, -1);
            });
            server.start();
            test.accept(server.getAddress().getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            server.stop(0);
        }
    }
}
