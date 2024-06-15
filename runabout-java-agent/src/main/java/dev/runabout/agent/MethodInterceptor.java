package dev.runabout.agent;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutListener;
import dev.runabout.RunaboutService;
import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;

class MethodInterceptor {

    public static ContextProvider contextProvider;
    public static RunaboutService runaboutService;
    public static RunaboutListener runaboutListener;

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
        System.out.println("Method intercepted");
        try {
            if (runaboutService != null) {
                String eventId = null;
                JsonObject properties = null;
                if (contextProvider != null) {
                    eventId = contextProvider.getEventId(clazz, method);
                    properties = contextProvider.getProperties(clazz, method);
                }
                System.out.println(runaboutService.createScenario(eventId, properties, args).toJsonObject().toJson());
            }
        } catch (Exception e) {
            runaboutListener.onError(e);
        }
    }
}
