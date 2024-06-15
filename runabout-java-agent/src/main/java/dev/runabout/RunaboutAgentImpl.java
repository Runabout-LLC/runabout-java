package dev.runabout;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class RunaboutAgentImpl implements RunaboutAgent {

    private Instrumentation instrumentation;

    private final String hookUrl;
    private final List<String> serverPath;
    private final ContextProvider contextProvider;
    private final RunaboutService runaboutService;
    private final AtomicBoolean installed = new AtomicBoolean(false);
    private final AtomicBoolean disabled = new AtomicBoolean(false);

    RunaboutAgentImpl(String hookUrl,
                      List<String> serverPath,
                      ContextProvider contextProvider,
                      RunaboutService runaboutService) {
        this.hookUrl = hookUrl;
        this.serverPath = serverPath;
        this.contextProvider = contextProvider;
        this.runaboutService = runaboutService;
    }

    @Override
    public void install() {
        if (!installed.get()) {
            this.instrumentation = ByteBuddyAgent.install();
            MethodInterceptor.setContextProvider(contextProvider);
            MethodInterceptor.setRunaboutService(runaboutService);
            register(hookUrl, serverPath);
            installed.set(true);
        }
//        else if (disabled.get()) {
//            disabled.set(false);
//        }
    }

    @Override
    public void disable() {
        disabled.set(true);
    }

    private static void handleCommand(final Command command) {
        final Map.Entry<Class<?>, Method> references = getReferences(command.getReference());
        final Class<?> targetClass = references.getKey();
        final Method targetMethod = references.getValue();

        final AsmVisitorWrapper visitor = Advice
                .to(MethodInterceptor.class)
                .on(targetMethod == null ? ElementMatchers.any() : ElementMatchers.is(targetMethod));
        try (final DynamicType.Unloaded<?> unloaded = new ByteBuddy().redefine(targetClass).visit(visitor).make()) {
            unloaded.load(targetClass.getClassLoader(),
                    ClassReloadingStrategy.fromInstalledAgent(ClassReloadingStrategy.Strategy.RETRANSFORMATION));
        }
    }

    private static void register(final String hookUrl, final List<String> serverPath) {
        // TODO call out to ingest.runabout.dev to register ourselves as a target for commands.
    }

    private static Map.Entry<Class<?>, Method> getReferences(final String identifier) {
        // TODO parse method string.
        return Map.entry(null,null);
    }
}
