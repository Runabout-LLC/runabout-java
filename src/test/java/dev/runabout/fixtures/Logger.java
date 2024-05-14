package dev.runabout.fixtures;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutService;
import dev.runabout.RunaboutServiceBuilder;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class Logger {

    private final PrintWriter printWriter;

    private final String event = UUID.randomUUID().toString();
    private final JsonObject properties = new JsonObject() {
        @Override
        public void put(String key, Boolean value) {

        }

        @Override
        public void put(String key, Number value) {

        }

        @Override
        public void put(String key, String value) {

        }

        @Override
        public void put(String key, JsonObject value) {

        }

        @Override
        public <T> void put(String key, Class<T> clazz, List<T> values) {

        }

        @Override
        public String toJson() {
            return "{ \"key\": \"value\" }";
        }
    };

    final RunaboutService<JsonObject> runaboutService = RunaboutServiceBuilder
            .getDefaultBuilder("dada")
            .setCallerClassBlacklist(Set.of(Logger.class))
            .build();

    public Logger(OutputStream outputStream) {
        this.printWriter = new PrintWriter(outputStream);
    }

    public void runaboutInfo(final Object... objects) {
        runaboutService.emitScenario(event, properties, objects);
        info(() -> runaboutService.createScenario(null, null, objects).toJson());
    }

    public void info(final Supplier<String> supplier) {
        printWriter.print(supplier.get());
        printWriter.flush();
    }
}
