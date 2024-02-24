package dev.runabout.fixtures;

import dev.runabout.RunaboutService;
import dev.runabout.RunaboutServiceBuilder;

import java.util.Set;
import java.util.function.Supplier;

public class Logger {

    final RunaboutService<?> runaboutService = RunaboutServiceBuilder
            .getDefaultBuilder()
            .setCallerClassBlacklist(Set.of(Logger.class))
            .build();

    public void runaboutInfo(final Object... objects) {
        info(() -> runaboutService.toRunaboutString(objects));
    }

    public void info(final Supplier<String> supplier) {
        System.out.println(supplier.get());
    }
}
