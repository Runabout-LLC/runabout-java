package dev.runabout.fixtures;

import dev.runabout.RunaboutService;
import dev.runabout.RunaboutServiceBuilder;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.function.Supplier;

public class Logger {

    private final PrintWriter printWriter;

//    final RunaboutService<?> runaboutService = RunaboutServiceBuilder
//            .getDefaultBuilder()
//            .setCallerClassBlacklist(Set.of(Logger.class))
//            .build();

    final RunaboutService<?> runaboutService = TestUtils.getDocumentRunaboutService();

    public Logger(OutputStream outputStream) {
        this.printWriter = new PrintWriter(outputStream);
    }

    public void runaboutInfo(final Object... objects) {
        info(() -> runaboutService.toRunaboutString(objects));
    }

    public void info(final Supplier<String> supplier) {
        printWriter.print(supplier.get());
        printWriter.flush();
    }
}
