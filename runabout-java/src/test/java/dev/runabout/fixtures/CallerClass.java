package dev.runabout.fixtures;

import dev.runabout.MethodResolver;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CallerClass {

    public void callLambda(MethodResolver callerSupplier, final AtomicReference<Method> method) {
        final Runnable runnable = () -> {
            method.set(callerSupplier.getMethod());
        };
        runnable.run();
    }

    public void callAnonymous(MethodResolver callerSupplier, final AtomicReference<Method> method) {
        new Runnable() {
            @Override
            public void run() {
                method.set(callerSupplier.getMethod());
            }
        }.run();
    }
}
