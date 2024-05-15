package dev.runabout.fixtures;

import dev.runabout.RunaboutAPIConfig;
import dev.runabout.RunaboutService;
import dev.runabout.RunaboutServiceBuilder;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.function.Supplier;

public class Logger {

    private final PrintWriter printWriter;

    final RunaboutService runaboutService = new RunaboutServiceBuilder("test", new RunaboutAPIConfig(null))
            .setCallerClassBlacklist(Set.of(Logger.class))
            .build();

    public Logger(OutputStream outputStream) {
        this.printWriter = new PrintWriter(outputStream);
    }

    public void runaboutInfo(final Object... objects) {
//        runaboutService.sendScenario(event, properties, objects);
        info(() -> runaboutService.createScenario(null, null, objects).toJsonObject().toJson());
    }

    public void info(final Supplier<String> supplier) {
        printWriter.print(supplier.get());
        printWriter.flush();
    }
}
