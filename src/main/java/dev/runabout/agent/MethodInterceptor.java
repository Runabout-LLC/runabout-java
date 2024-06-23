package dev.runabout.agent;

import java.lang.reflect.Method;

// TODO javadoc
@FunctionalInterface
public interface MethodInterceptor {
    void accept(Class<?> clazz, Method method, Object[] args);
}
