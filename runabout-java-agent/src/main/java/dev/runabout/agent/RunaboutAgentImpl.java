package dev.runabout.agent;

import dev.runabout.RunaboutException;
import dev.runabout.RunaboutListener;
import dev.runabout.RunaboutService;
import dev.runabout.RunaboutUtils;
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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

class RunaboutAgentImpl implements RunaboutAgent {

    private static final ClassReloadingStrategy.Strategy STRATEGY = ClassReloadingStrategy.Strategy.RETRANSFORMATION;

    private Instrumentation instrumentation;

    private final int port;
    private final String hookUrl;
    private final List<String> serverPath;
    private final CommandServer commandServer;
    private final ContextProvider contextProvider;
    private final RunaboutService runaboutService;
    private final RunaboutListener runaboutListener;


    private final AtomicBoolean installed = new AtomicBoolean(false);

    RunaboutAgentImpl(int port,
                      String hookUrl,
                      List<String> serverPath,
                      ContextProvider contextProvider,
                      RunaboutService runaboutService,
                      RunaboutListener runaboutListener) {
        this.port = port;
        this.hookUrl = hookUrl;
        this.serverPath = serverPath;
        this.contextProvider = contextProvider;
        this.runaboutService = runaboutService;
        this.runaboutListener = runaboutListener;
        this.commandServer = new CommandServer(port, runaboutListener, this::handleCommand);
    }

    @Override
    public void install() {
        if (!installed.get()) {
            commandServer.start();
            this.instrumentation = ByteBuddyAgent.install();
            MethodInterceptor.setRunaboutListener(runaboutListener);
            MethodInterceptor.setContextProvider(contextProvider);
            MethodInterceptor.setRunaboutService(runaboutService);
            register(hookUrl, serverPath);
            installed.set(true);
        }
    }

    @Override
    public void disable() {
        // TODO
    }

    private void handleCommand(final Command command) throws RunaboutException {

        Objects.requireNonNull(command.getReference(), "Invalid command, missing reference.");

        final Pair<Class<?>, Method> references = getReferences(command.getReference());
        final Class<?> clazz = references.left;
        final Method method = references.right;

        switch (command.getAction()) {
            case ENABLE:
                loadIntercepter(clazz, method);
                break;
            case DISABLE:
                restore(clazz);
                break;
            default:
                throw new RunaboutException("Invalid command action: " + command.getAction().name());
        }
    }

    private static void loadIntercepter(final Class<?> clazz, final Method method) {
        final AsmVisitorWrapper visitor = Advice
                .to(MethodInterceptor.class)
                .on(method == null ? ElementMatchers.not(ElementMatchers.isConstructor()) : ElementMatchers.is(method));
        try (final DynamicType.Unloaded<?> unloaded = new ByteBuddy().redefine(clazz).visit(visitor).make()) {
            unloaded.load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent(STRATEGY));
        }
    }

    private static void restore(final Class<?> clazz) {
        try (final DynamicType.Unloaded<?> unloaded = new ByteBuddy().redefine(clazz).make()) {
            unloaded.load(clazz.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent(STRATEGY));
        }
    }

    private static void register(final String hookUrl, final List<String> serverPath) {
        // TODO call out to ingest.runabout.dev to register ourselves as a target for commands.
    }

    private static Pair<Class<?>, Method> getReferences(final String identifier) {

        Method method = null;
        if (identifier.contains("#")) {
            method = RunaboutUtils.runaboutStringToMethod(identifier);
        }

        final Class<?> clazz = Optional.ofNullable(method)
                .map(Method::getDeclaringClass)
                .orElseGet(() -> (Class) RunaboutUtils.getClass(identifier)); // TODO confirm this is safe.

        return new Pair<>(clazz, method);
    }

    private static final class Pair<T,U> {

        final T left;
        final U right;

        private Pair(T left, U right) {
            this.left = left;
            this.right = right;
        }
    }
}
