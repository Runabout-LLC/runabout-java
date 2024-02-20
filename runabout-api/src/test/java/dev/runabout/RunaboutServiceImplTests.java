package dev.runabout;

import org.junit.jupiter.api.Test;

import java.util.List;

public class RunaboutServiceImplTests {

    @Test
    void comprehensiveTest() {
        final RunaboutService<JsonObject> runaboutService = RunaboutService.getService();
        final JsonObject jsonObject = runaboutService.toRunaboutJson("testString", 8L, List.of("a", "b", "c"));
        System.out.println(jsonObject.toJson());
    }
}