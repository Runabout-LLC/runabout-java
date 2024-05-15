package dev.runabout;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class RunaboutServiceBuilderTests {

    @Test
    void testCallerBlacklistValidation() {
        Assertions.assertThrows(RunaboutException.class, () -> new RunaboutServiceBuilder("test", new RunaboutAPIConfig(null))
                .setCallerClassBlacklist(Set.of())
                .setCallerSupplier(() -> null)
                .build());
    }
}
