package dev.runabout.agent;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutListener;
import dev.runabout.RunaboutService;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;

class MethodInterceptor {

    // TODO maybe hold onto agent instead of all the interfaces on the agent?
    private static CommandStore commandStore;
    private static ContextProvider contextProvider;
    private static RunaboutService runaboutService;
    private static RunaboutListener runaboutListener;

    private static boolean disabled = false;

    public static CommandStore getCommandStore() {
        return commandStore;
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

    static void setCommandStore(CommandStore commandStore) {
        if (MethodInterceptor.commandStore != null) {
            throw new IllegalStateException("MethodInterceptor.CommandStore can only be set once.");
        }
        MethodInterceptor.commandStore = commandStore;
    }

    static void setContextProvider(ContextProvider contextProvider) {
        if (MethodInterceptor.contextProvider != null) {
            throw new IllegalStateException("MethodInterceptor.ContextProvider can only be set once.");
        }
        MethodInterceptor.contextProvider = contextProvider;
    }

    static void setRunaboutService(RunaboutService runaboutService) {
        if (MethodInterceptor.runaboutService != null) {
            throw new IllegalStateException("MethodInterceptor.RunaboutService can only be set once.");
        }
        MethodInterceptor.runaboutService = runaboutService;
    }

    static void setRunaboutListener(RunaboutListener runaboutListener) {
        if (MethodInterceptor.runaboutListener != null) {
            throw new IllegalStateException("MethodInterceptor.RunaboutListener can only be set once.");
        }
        MethodInterceptor.runaboutListener = runaboutListener;
    }

    static void enable() {
        disabled = false;
    }

    static void disable() {
        disabled = true;
    }

    @Advice.OnMethodEnter
    public static void onMethodEnter(@Advice.Origin Method method,
                                     @Advice.Origin Class<?> clazz,
                                     @Advice.This(optional = true) Object thisObject,
                                     @Advice.AllArguments Object[] args) {

        if (MethodInterceptor.isDisabled()) {
            return;
        }

        System.out.println("Method intercepted");
        final RunaboutListener runaboutListener = MethodInterceptor.getRunaboutListener();

        try {
            final RunaboutService runaboutService = MethodInterceptor.getRunaboutService();
            if (runaboutService != null) {

                final Instant now = Instant.now();
                final Long timeout = MethodInterceptor.getCommandStore().get(clazz, method);

                if (timeout != null && timeout > now.toEpochMilli()) {

                    String eventId = null;
                    JsonObject properties = null;
                    final ContextProvider contextProvider = MethodInterceptor.getContextProvider();

                    if (contextProvider != null) {
                        eventId = contextProvider.getEventId(clazz, method);
                        properties = contextProvider.getProperties(clazz, method);
                    }

                    Object[] allInstances = args;

                    if (!Modifier.isStatic(method.getModifiers())) {
                        System.out.println("Method is not static");
                        allInstances = new Object[args.length + 1];
                        allInstances[0] = thisObject;
                        System.arraycopy(args, 0, allInstances, 1, args.length);
                    }

                    System.out.println(runaboutService.createScenario(eventId, properties, allInstances).toJsonObject().toJson()); // TODO save instead of printing
                }
            }
        } catch (Exception e) {
            runaboutListener.onError(e);
        }
    }
}
