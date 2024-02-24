package dev.runabout.packagetests;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutService;
import dev.runabout.fixtures.ConcreteClass1;
import dev.runabout.fixtures.ConcreteClass2;
import dev.runabout.fixtures.Logic1;
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

    @Test
    void runaboutLoggerTest() {
        final Logic1 logic1 = new Logic1(Map.of("ethan", "bond", "john", "adams"));
        final ConcreteClass1 concreteClass1 = new ConcreteClass1("Ethan", Set.of("aa", "bb"));
        final ConcreteClass2 concreteClass2 = new ConcreteClass2(1, Map.of("a", "b", "c", "d"));
        logic1.printValuesLambdaLogger(concreteClass1, concreteClass2);
    }
}