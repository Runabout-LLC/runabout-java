package dev.runabout;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;

class MethodInterceptor {

    private static ContextProvider contextProvider;
    private static RunaboutService runaboutService;
    private static RunaboutListener runaboutListener;

    public static void setContextProvider(ContextProvider contextProvider) {
        MethodInterceptor.contextProvider = contextProvider;
    }

    public static void setRunaboutService(RunaboutService runaboutService) {
        MethodInterceptor.runaboutService = runaboutService;
    }

    public static void setRunaboutListener(RunaboutListener runaboutListener) {
        MethodInterceptor.runaboutListener = runaboutListener;
    }

    @Advice.OnMethodEnter
    public static void onMethodEnter(@Advice.Origin Method method,
                                     @Advice.Origin Class<?> clazz,
                                     @Advice.AllArguments Object[] args) {
        try {
            if (runaboutService != null) {
                String eventId = null;
                JsonObject properties = null;
                if (contextProvider != null) {
                    eventId = contextProvider.getEventId(clazz, method);
                    properties = contextProvider.getProperties(clazz, method);
                }
                runaboutService.saveScenario(eventId, properties, args);
            }
        } catch (Exception e) {
            runaboutListener.onError(e);
        }
    }
}
