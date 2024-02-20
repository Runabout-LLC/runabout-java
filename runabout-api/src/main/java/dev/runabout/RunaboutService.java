package dev.runabout;

import dev.runabout.json.JsonObject;

import java.lang.reflect.Method;
import java.util.Set;

public interface RunaboutService<T extends JsonObject> {

    static RunaboutService<JsonObject> getService() {
        return RunaboutServiceBuilder.getDefaultBuilder().build();
    }

    RunaboutInput serialize(final Object object);

    T toRunaboutJson(final Object... objects);

    T toRunaboutJson(final Method method, final Object... objects);

    default String toRunaboutString(final Object... objects) {
        return toRunaboutJson(objects).toJson();
    }

    default String toRunaboutString(final Method method, final Object... objects) {
        return toRunaboutJson(method, objects).toJson();
    }
}
