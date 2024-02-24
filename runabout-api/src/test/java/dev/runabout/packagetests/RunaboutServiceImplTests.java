package dev.runabout.packagetests;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutService;
import dev.runabout.fixtures.ConcreteClass1;
import dev.runabout.fixtures.ConcreteClass2;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class RunaboutServiceImplTests {

    @Test
    void comprehensiveTest() {
        final RunaboutService<JsonObject> runaboutService = RunaboutService.getService();
        final JsonObject jsonObject = runaboutService.toRunaboutJson(
                new ConcreteClass1("Ethan", Set.of("aa", "bb")),
                new ConcreteClass2(1, Map.of("a", "b", "c", "d"))
        );
        System.out.println(jsonObject.toJson());
    }
}