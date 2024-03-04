package dev.runabout;

import dev.runabout.fixtures.CallerClass;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultCallerSupplierTests {

    @Test
    void testIgnoreLambdaMethod() {
        final DefaultCallerSupplier callerSupplier = new DefaultCallerSupplier(Set.of());
        final AtomicReference<Method> method = new AtomicReference<>();
        new CallerClass().callLambda(callerSupplier, method);
        System.out.println(method.get());
    }

    @Test
    void testIgnoreAnonymousClass() {
        final DefaultCallerSupplier callerSupplier = new DefaultCallerSupplier(Set.of());
        final AtomicReference<Method> method = new AtomicReference<>();
        new CallerClass().callAnonymous(callerSupplier, method);
        System.out.println(method.get());
    }
}
