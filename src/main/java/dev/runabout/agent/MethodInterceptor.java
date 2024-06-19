package dev.runabout.agent;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutListener;
import dev.runabout.RunaboutService;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class MethodInterceptor {

    // TODO maybe hold onto agent instead of all the interfaces on the agent?
    private static final InstructionStore instructionStore = new InstructionStore();

    private static ContextProvider contextProvider;
    private static RunaboutService runaboutService;
    private static RunaboutListener runaboutListener;

    private static boolean disabled = false;

    public static long getTimeout(final Class<?> clazz, final Method method) {
        return instructionStore.getTimeout(clazz, method);
    }

    public static ContextProvider getContextProvider() {
        return contextProvider;
    }

    public static RunaboutService getRunaboutService() {
        return runaboutService;
    }

    public static RunaboutListener getRunaboutListener() {
        return runaboutListener;
    }

    public static boolean isDisabled() {
        return disabled;
    }

    protected static void setContextProvider(ContextProvider contextProvider) {
        if (MethodInterceptor.contextProvider != null) {
            throw new IllegalStateException("MethodInterceptor.ContextProvider can only be set once.");
        }
        MethodInterceptor.contextProvider = contextProvider;
    }

    protected static void setRunaboutService(RunaboutService runaboutService) {
        if (MethodInterceptor.runaboutService != null) {
            throw new IllegalStateException("MethodInterceptor.RunaboutService can only be set once.");
        }
        MethodInterceptor.runaboutService = runaboutService;
    }

    protected static void setRunaboutListener(RunaboutListener runaboutListener) {
        if (MethodInterceptor.runaboutListener != null) {
            throw new IllegalStateException("MethodInterceptor.RunaboutListener can only be set once.");
        }
        MethodInterceptor.runaboutListener = runaboutListener;
    }

    protected static void updateInstructionStore(final Set<Instruction> instructions,
                                                 final BiConsumer<Class<?>, Method> removeInstructions,
                                                 final BiConsumer<Class<?>, Method> installInstructions) {
        instructionStore.update(instructions, removeInstructions, installInstructions, runaboutListener);
    }

    protected static void enable() {
        disabled = false;
    }

    protected static void disable() {
        disabled = true;
    }

    @Advice.OnMethodEnter(inline = false)
    public static void onMethodEnter(@Advice.Origin Method method,
                                     @Advice.Origin Class<?> clazz,
                                     @Advice.AllArguments(includeSelf = true) Object[] args) {

        if (disabled) {
            return;
        }

        try {
            if (runaboutService != null) {

                final Instant now = Instant.now();
                final Long timeout = instructionStore.getTimeout(clazz, method);

                if (timeout != null && timeout > now.toEpochMilli()) {

                    String eventId = null;
                    JsonObject properties = null;

                    if (contextProvider != null) {
                        eventId = contextProvider.getEventId(clazz, method);
                        properties = contextProvider.getProperties(clazz, method);
                    }

                    runaboutService.saveScenario(method, eventId, properties, args);
                }
            }
        } catch (Exception e) {
            runaboutListener.onError(e);
        }
    }
}
