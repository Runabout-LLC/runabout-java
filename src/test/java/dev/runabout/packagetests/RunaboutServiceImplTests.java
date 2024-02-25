package dev.runabout.packagetests;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutInput;
import dev.runabout.RunaboutService;
import dev.runabout.fixtures.ConcreteClass1;
import dev.runabout.fixtures.ConcreteClass2;
import dev.runabout.fixtures.Logic1;
import dev.runabout.fixtures.UnknownClass1;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Logic1 logic1 = new Logic1(outputStream, Map.of("ethan", "bond", "john", "adams"));
        final ConcreteClass1 concreteClass1 = new ConcreteClass1("Ethan", Set.of("aa", "bb"));
        final ConcreteClass2 concreteClass2 = new ConcreteClass2(1, Map.of("a", "b", "c", "d"));
        final String output = logic1.concatValuesLayerLogger(concreteClass1, concreteClass2);

        final String loggerOutput = outputStream.toString(StandardCharsets.UTF_8);
        System.out.println(loggerOutput);
        final Document document = Document.parse(loggerOutput);
        System.out.println(document.toJson()); // TODO assert
    }

    @Test
    void testNoSerializerFound() {

        final UnknownClass1 object = new UnknownClass1("field1", "field2");
        final RunaboutService<JsonObject> runaboutService = RunaboutService.getService();

        // Test serialize method
        final RunaboutInput runaboutInput = runaboutService.serialize(object);
        Assertions.assertEquals("", runaboutInput.getEval());
        Assertions.assertTrue(runaboutInput.getDependencies().isEmpty());

        // Test full runabout json
        final JsonObject jsonObject = runaboutService.toRunaboutJson(new Object());

        // Document for assertions
        final Document document = Document.parse(jsonObject.toJson());
        final List<Document> inputs = document.getList("inputs", Document.class);
        Assertions.assertEquals(1, inputs.size());
        final Document input = inputs.get(0);
        Assertions.assertEquals("", input.getString("eval"));
        Assertions.assertTrue(input.get("dependencies", List.class).isEmpty());
    }
}