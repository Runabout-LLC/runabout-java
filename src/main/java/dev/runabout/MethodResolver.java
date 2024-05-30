package dev.runabout;

import dev.runabout.utils.RunaboutUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Runabout interface for determining the caller method when using the {@link RunaboutService}.
 * This interface is used to ensure the correct method is recorded when saving a runabout scenario.
 * The library comes with a default implementation and a builder to meet most use cases.
 * See {@link MethodResolverBuilder} for more information.
 */
public interface MethodResolver {

    /**
     * Determines the method further down the stack that called into the RunaboutService and should be
     * used in the resulting {@link RunaboutScenario}.
     *
     * @return The method that the scenario will run.
     */
    Method getMethod();

    /**
     * Gets the method as a string in the format expected by Runabout. Only override this method if you have
     * a custom method format that Runabout can understand.
     *
     * @return A string identifying the method.
     */
    default String getSerializedMethod() {
        final Method method = Objects.requireNonNull(getMethod(), "Caller method cannot be null.");
        return RunaboutUtils.methodToRunaboutString(method);
    }
}
