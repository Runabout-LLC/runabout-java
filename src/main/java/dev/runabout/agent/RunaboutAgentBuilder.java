package dev.runabout.agent;

import dev.runabout.RunaboutService;
import dev.runabout.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * TODO
 */
public class RunaboutAgentBuilder {

    private ContextProvider contextProvider;
    private MethodInterceptor methodInterceptor;

    private final RunaboutService runaboutService;

    public RunaboutAgentBuilder(RunaboutService runaboutService) {
        this.runaboutService = Objects.requireNonNull(runaboutService, "RunaboutService cannot be null");
    }

    public RunaboutAgentBuilder setContextProvider(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        return this;
    }

    public RunaboutAgentBuilder setMethodInterceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
        return this;
    }

    public RunaboutAgent build() {

        final MethodInterceptor methodInterceptorFinal = Optional.ofNullable(methodInterceptor)
                .orElseGet(() -> new RunaboutMethodInterceptor(contextProvider, runaboutService));

        return new RunaboutAgentImpl(runaboutService, methodInterceptor);
    }
}
