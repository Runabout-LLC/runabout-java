package dev.runabout;

import dev.runabout.fixtures.EnabledClass;
import dev.runabout.fixtures.EnabledImproperClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RunaboutEnabledTests {

    @Test
    void testValid() {
        final EnabledClass enabledClass = new EnabledClass(1, "name", true, List.of("a", "b", "c"));
        final RunaboutService service = RunaboutService.getService("test", null);
        final RunaboutInput input = service.serialize(enabledClass);
        Assertions.assertEquals("new EnabledClass((int) 1, \"name\", (boolean) true, new ArrayList<>() {{ add(\"a\"); add(\"b\"); add(\"c\"); }})",
                input.getEval());
        Assertions.assertEquals(2, input.getDependencies().size());
        Assertions.assertTrue(input.getDependencies().contains(ArrayList.class.getCanonicalName()));
        Assertions.assertTrue(input.getDependencies().contains(EnabledClass.class.getCanonicalName()));
    }

    @Test
    void testInvalidFallback() {
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final EnabledImproperClass enabledImproperClass = new EnabledImproperClass(1, "name", true, List.of("a", "b", "c"));
        final RunaboutService service = new RunaboutServiceBuilder("test")
                .setRunaboutApi(new RunaboutApiBuilder(() -> null).build())
                .setListener(error::set)
                .build();
        final RunaboutInput input = service.serialize(enabledImproperClass);
        Assertions.assertEquals("TEST", input.getEval());
        Assertions.assertEquals(1, input.getDependencies().size());
        Assertions.assertTrue(input.getDependencies().contains("TEST"));
        Assertions.assertNotNull(error.get());
    }
}
