package dev.runabout.agent;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutApiBuilder;
import dev.runabout.RunaboutService;
import dev.runabout.RunaboutServiceBuilder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        final RunaboutService service = new RunaboutServiceBuilder("test")
                .setRunaboutApi(new RunaboutApiBuilder(() -> "hmmm").build())
                .setListener(throwable -> System.out.println("Error caught in service"))
                .build();

        RunaboutAgent agent = new RunaboutAgentImpl(8086,
                "localhost:8086/commands", List.of("runabout", "dev", "local"), new ContextProvider() {

            @Override
            public String getEventId(Class<?> clazz, Method method) {
                return UUID.randomUUID().toString();
            }

            @Override
            public JsonObject getProperties(Class<?> clazz, Method method) {
                return null;
            }
        }, service, throwable -> System.out.println("Error caught"));
        agent.install();
    }


}
