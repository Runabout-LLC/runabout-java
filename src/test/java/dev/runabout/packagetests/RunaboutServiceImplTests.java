package dev.runabout.packagetests;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutInput;
import dev.runabout.RunaboutService;
import dev.runabout.RunaboutServiceBuilder;
import dev.runabout.fixtures.ConcreteClass1;
import dev.runabout.fixtures.ConcreteClass2;
import dev.runabout.fixtures.Logic1;
import dev.runabout.fixtures.ThrowsClass1;
import dev.runabout.fixtures.ThrowsClass2;
import dev.runabout.fixtures.UnknownClass1;
import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunaboutServiceImplTests {

    @Test
    void runaboutLoggerTest() {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Logic1 logic1 = new Logic1(outputStream, Map.of("george", "washington", "john", "adams"));
        final ConcreteClass1 concreteClass1 = new ConcreteClass1("george", Set.of("aa", "bb"));
        final ConcreteClass2 concreteClass2 = new ConcreteClass2(1, Map.of("a", "b", "c", "d"));

        logic1.concatValuesLayerLogger(concreteClass1, concreteClass2);

        final String loggerOutput = outputStream.toString(StandardCharsets.UTF_8);
        final Document document = Document.parse(loggerOutput);

        final String method = document.getString("method");
        Assertions.assertTrue(method.contains("Logic1.concatValuesLayerLogger"));

        final List<Document> inputs = document.getList("inputs", Document.class);
        Assertions.assertEquals(3, inputs.size());
        assertJsonString(inputs.get(0), Logic1.class, "", Collections.emptySet());
        assertJsonString(inputs.get(1), ConcreteClass1.class, "new ConcreteClass1(", Set.of(ConcreteClass1.class, HashSet.class));
        assertJsonString(inputs.get(2), ConcreteClass2.class, "new ConcreteClass2(", Set.of(ConcreteClass2.class, HashMap.class));
    }

    @Test
    void runaboutLoggerLambdaTest() {

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Logic1 logic1 = new Logic1(outputStream, Map.of("george", "washington", "john", "adams"));
        final ConcreteClass1 concreteClass1 = new ConcreteClass1("george", Set.of("aa", "bb"));
        final ConcreteClass2 concreteClass2 = new ConcreteClass2(1, Map.of("a", "b", "c", "d"));

        logic1.concatValuesLambdaLogger(concreteClass1, concreteClass2);

        final String loggerOutput = outputStream.toString(StandardCharsets.UTF_8);
        final Document document = Document.parse(loggerOutput);

        final String method = document.getString("method");
        Assertions.assertTrue(method.contains("Logic1.concatValuesLambdaLogger"));

        final List<Document> inputs = document.getList("inputs", Document.class);
        Assertions.assertEquals(3, inputs.size());
        assertJsonString(inputs.get(0), Logic1.class, "", Collections.emptySet());
        assertJsonString(inputs.get(1), ConcreteClass1.class, "new ConcreteClass1(", Set.of(ConcreteClass1.class, HashSet.class));
        assertJsonString(inputs.get(2), ConcreteClass2.class, "new ConcreteClass2(", Set.of(ConcreteClass2.class, HashMap.class));
    }

    private void assertJsonString(Document document, Class<?> expectedType,
                                  String expectedEvalPrefix, Set<Class<?>> expectedDependencies) {

        final String type = document.getString("type");
        Assertions.assertEquals(expectedType.getCanonicalName(), type);

        final String eval = document.getString("eval");
        Assertions.assertTrue(eval.startsWith(expectedEvalPrefix));

        final List<String> dependencies = document.getList("dependencies", String.class);
        Assertions.assertEquals(expectedDependencies.size(), dependencies.size());
        for (Class<?> expectedDependency : expectedDependencies) {
            Assertions.assertTrue(dependencies.contains(expectedDependency.getCanonicalName()));
        }
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

    @Test
    void testInstanceSerializerThrows() {
        final List<Throwable> thrown = new ArrayList<>();
        final RunaboutService<JsonObject> runaboutService = RunaboutServiceBuilder.getDefaultBuilder()
                .setThrowableConsumer(thrown::add)
                .build();
        final ThrowsClass1 throwsInstance = new ThrowsClass1("george", "washington");
        final JsonObject jsonObject = runaboutService.toRunaboutJson(throwsInstance);
        final Document document = Document.parse(jsonObject.toJson());
        final Document input = document.getList("inputs", Document.class).get(0);
        Assertions.assertEquals("", input.getString("eval"));
        Assertions.assertTrue(input.get("dependencies", List.class).isEmpty());
        Assertions.assertEquals(1, thrown.size());
        Assertions.assertEquals(ThrowsClass1.EXCEPTION_MESSAGE, thrown.get(0).getMessage());
    }

    @Test
    void testGenericSerializerThrows() {
        final List<Throwable> thrown = new ArrayList<>();
        final RunaboutService<JsonObject> runaboutService = RunaboutServiceBuilder.getDefaultBuilder()
                .setThrowableConsumer(thrown::add)
                .build();
        final ThrowsClass2 throwsInstance = new ThrowsClass2("george", "washington");
        final JsonObject jsonObject = runaboutService.toRunaboutJson(throwsInstance);
        final Document document = Document.parse(jsonObject.toJson());
        final Document input = document.getList("inputs", Document.class).get(0);
        Assertions.assertEquals("", input.getString("eval"));
        Assertions.assertTrue(input.get("dependencies", List.class).isEmpty());
        Assertions.assertEquals(1, thrown.size());
        Assertions.assertEquals(ThrowsClass2.EXCEPTION_MESSAGE, thrown.get(0).getMessage());
    }
}