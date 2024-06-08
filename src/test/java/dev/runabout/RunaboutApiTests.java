package dev.runabout;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class RunaboutApiTests {

    private static final String TEST_TOKEN = "test-token";

    // Test post with defaults
    @Test
    void basicScenarioTest() {
        final AtomicReference<String> token = new AtomicReference<>();
        final AtomicReference<Document> body = new AtomicReference<>();
        withLocalServer(
            (headers, s) -> {
                System.out.println("headers: " + headers);
                token.set(headers.get("Authorization").get(0).replace("Bearer ", ""));
                body.set(Document.parse(s));
                return "";
            },
            port -> {
                final String url = "http://localhost:" + port + "/" + RunaboutConstants.SCENARIOS_KEY;
                final RunaboutApi api = new RunaboutApiBuilder(() -> TEST_TOKEN)
                        .setUri(URI.create(url))
                        .setListener(error -> Assertions.fail())
                        .build();
                final RunaboutScenario scenario = new RunaboutScenario("method", "event00", "dev",
                        Timestamp.from(Instant.now()), new JsonObjectImpl().put("key", "value"), List.of(
                        new RunaboutInstance("type", "eval", Set.of("dep1", "dep2"))));
                api.ingestScenario(scenario);
                Awaitility.await().atMost(Duration.ofSeconds(30))
                        .pollInterval(Duration.ofMillis(1000))
                        .until(() -> body.get() != null);
                Assertions.assertEquals(TEST_TOKEN, token.get());
                Assertions.assertEquals(new Document(RunaboutConstants.SCENARIOS_KEY, List.of(Document.parse(scenario.toJsonObject().toJson()))), body.get());
            });
    }

    // TODO Test post failure handled correctly


    private static void withLocalServer(BiFunction<Headers,String,String> logic, Consumer<Integer> test) {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8085), 0);
            server.createContext("/" + RunaboutConstants.SCENARIOS_KEY, httpExchange -> {
                final Headers headers = httpExchange.getRequestHeaders();
                final String body = new String(httpExchange.getRequestBody().readAllBytes());
                final String response = logic.apply(headers, body);
                httpExchange.sendResponseHeaders(200, response.length());
                final OutputStream out = httpExchange.getResponseBody();
                out.write(response.getBytes());
                out.close();
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
