package dev.runabout;

import dev.runabout.utils.RunaboutUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * TODO interface for determining the caller method to use in the Runabout input.
 */
public interface MethodResolver extends Supplier<Method> {

    //
    // TODO document
    //
    default String getSerializedMethod() {
        final Method method = Objects.requireNonNull(get(), "Caller method cannot be null.");
        return RunaboutUtils.methodToRunaboutString(method);
    }
}
