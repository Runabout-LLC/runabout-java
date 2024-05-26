package dev.runabout;

import dev.runabout.fixtures.CallerClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class MethodResolverImplTests {

    @Test
    void testIgnoreLambdaMethod() {
        final MethodResolverImpl callerSupplier = new MethodResolverImpl(s -> true);
        final AtomicReference<Method> method = new AtomicReference<>();
        new CallerClass().callLambda(callerSupplier, method);
        Assertions.assertNotNull(method.get());
        final String signature = method.get().toString();
        Assertions.assertEquals("public void dev.runabout.fixtures.CallerClass.callLambda(java.util.function.Supplier,java.util.concurrent.atomic.AtomicReference)",
                signature);
    }

    @Test
    void testIgnoreAnonymousClass() {
        final MethodResolverImpl callerSupplier = new MethodResolverImpl(s -> true);
        final AtomicReference<Method> method = new AtomicReference<>();
        new CallerClass().callAnonymous(callerSupplier, method);
        Assertions.assertNotNull(method.get());
        final String signature = method.get().toString();
        Assertions.assertEquals("public void dev.runabout.fixtures.CallerClass.callAnonymous(java.util.function.Supplier,java.util.concurrent.atomic.AtomicReference)",
                signature);
    }
}
