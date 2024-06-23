package dev.runabout.agent;

import dev.runabout.JsonObject;
import dev.runabout.RunaboutService;

import java.lang.reflect.Method;

public class RunaboutMethodInterceptor implements MethodInterceptor {

    private final ContextProvider contextProvider;
    private final RunaboutService runaboutService;

    public RunaboutMethodInterceptor(ContextProvider contextProvider, RunaboutService runaboutService) {
        this.contextProvider = contextProvider;
        this.runaboutService = runaboutService;
    }

    @Override
    public void accept(Class<?> clazz, Method method, Object[] args) {
        if (runaboutService != null) {
            try {
                String eventId = null;
                JsonObject properties = null;

                if (contextProvider != null) {
                    eventId = contextProvider.getEventId(clazz, method);
                    properties = contextProvider.getProperties(clazz, method);
                }

                runaboutService.saveScenario(method, eventId, properties, args);
            } catch (Exception e) {
                runaboutService.getListener().onError(e);
            }
        }
    }
}
