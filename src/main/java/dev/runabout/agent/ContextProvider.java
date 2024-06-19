package dev.runabout.agent;

import dev.runabout.JsonObject;
import dev.runabout.annotations.Nullable;

import java.lang.reflect.Method;

public interface ContextProvider {

    @Nullable
    String getEventId(Class<?> clazz, Method method);

    @Nullable
    JsonObject getProperties(Class<?> clazz, Method method);
}