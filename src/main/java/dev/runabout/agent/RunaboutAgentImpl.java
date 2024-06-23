package dev.runabout.agent;

import dev.runabout.RunaboutService;
import dev.runabout.annotations.Nullable;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class RunaboutAgentImpl implements RunaboutAgent {

    private static final ClassReloadingStrategy.Strategy STRATEGY = ClassReloadingStrategy.Strategy.RETRANSFORMATION;

    private final RunaboutService runaboutService;
    private final MethodInterceptor methodInterceptor;

    private final AtomicBoolean installed = new AtomicBoolean(false);

    RunaboutAgentImpl(RunaboutService runaboutService, MethodInterceptor methodInterceptor) {
        this.runaboutService = runaboutService;
        this.methodInterceptor = methodInterceptor;
    }

    @Override
    public void install() {
        if (!installed.get()) {
            ByteBuddyAgent.install();
            runaboutService.getRunaboutApi(); // TODO register via RunaboutAPI
            installed.set(true);
            MethodInterceptorWrapper.setInterceptor(methodInterceptor);
        }
    }

    @Override
    public void enable() {
        MethodInterceptorWrapper.enable();
    }

    @Override
    public void refresh() {
        final String project = runaboutService.getProjectName();
        final Set<Instruction> instructions = runaboutService.getRunaboutApi().getLatestInstructions(project);
        refresh(instructions);
    }

    @Override
    public void refresh(Set<Instruction> instructions) {
        MethodInterceptorWrapper.updateInstructionStore(instructions,
                RunaboutAgentImpl::loadInterceptor,
                RunaboutAgentImpl::restore,
                runaboutService.getListener());
    }

    @Override
    public void disable() {
        MethodInterceptorWrapper.disable();
    }


    private static void loadInterceptor(final Class<?> clazz, @Nullable final Method method) {
        final AsmVisitorWrapper visitor = Advice
                .to(MethodInterceptorWrapper.class)
                .on(method == null ? ElementMatchers.not(ElementMatchers.isConstructor()) : ElementMatchers.is(method));
        try (final DynamicType.Unloaded<?> unloaded = new ByteBuddy().redefine(clazz).visit(visitor).make()) {
            unloaded.load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent(STRATEGY));
        }
    }

    private static void restore(final Class<?> clazz, @Nullable final Method method) {
        final DynamicType.Builder<?> builder = new ByteBuddy().redefine(clazz);
        if (method != null) {
            builder.method(ElementMatchers.is(method));
        }
        try (final DynamicType.Unloaded<?> unloaded = builder.make()) {
            unloaded.load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent(STRATEGY));
        }
    }
}
