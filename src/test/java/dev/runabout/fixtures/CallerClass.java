package dev.runabout.fixtures;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CallerClass {

    public void callLambda(Supplier<Method> callerSupplier, final AtomicReference<Method> method) {
        final Runnable runnable = () -> {
            method.set(callerSupplier.get());
        };
        runnable.run();
    }

    public void callAnonymous(Supplier<Method> callerSupplier, final AtomicReference<Method> method) {
        new Runnable() {
            @Override
            public void run() {
                method.set(callerSupplier.get());
            }
        };
    }
}
