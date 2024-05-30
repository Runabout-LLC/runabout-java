package dev.runabout;

import java.util.Set;
import java.util.function.Predicate;

public class MethodResolverBuilder {

    private Set<Class<?>> callerClassBlacklist;
    private Predicate<StackWalker.StackFrame> stackFramePredicate;

    /**
     * Sets the caller class blacklist for the MethodResolver.
     * If provided, the blacklist will be used to filter out classes when determining the caller in the MethodResolver.
     * A check for the existence in this set will be used in conjunction with the stack frame predicate.
     *
     * @param callerClassBlacklist Set of classes to avoid when resolving the method.
     * @return The MethodResolverBuilder.
     */
    public MethodResolverBuilder setCallerClassBlacklist(Set<Class<?>> callerClassBlacklist) {
        this.callerClassBlacklist = callerClassBlacklist;
        return this;
    }

    /**
     * Sets the stack frame predicate for the MethodResolver.
     * If provided, the predicate must only return true for a stack frames that should be considered when determining
     * the method the scenario is being built over. The first stack frame to pass the predicate will be used.
     *
     * @param stackFramePredicate The stack frame predicate.
     * @return The MethodResolverBuilder.
     */
    public MethodResolverBuilder setStackFramePredicate(Predicate<StackWalker.StackFrame> stackFramePredicate) {
        this.stackFramePredicate = stackFramePredicate;
        return this;
    }

    public MethodResolver build() {

        Predicate<StackWalker.StackFrame> predicate = s -> true;

        if (stackFramePredicate != null) {
            predicate = predicate.and(stackFramePredicate);
        }

        if (callerClassBlacklist != null && !callerClassBlacklist.isEmpty()) {
            predicate = predicate.and(stackFrame -> {
                final Class<?> declaringClass = stackFrame.getDeclaringClass();
                return !callerClassBlacklist.contains(declaringClass);
            });
        }

        return new MethodResolverImpl(predicate);
    }
}
