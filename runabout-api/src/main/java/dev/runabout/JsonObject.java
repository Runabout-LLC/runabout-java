package dev.runabout;

import java.util.List;

public interface JsonObject {

    void put(String key, Boolean value);

    void put(String key, Number value);

    void put(String key, String value);

    void put(String key, JsonObject value);

    <T> void put(String key, Class<T> clazz, List<T> values);

    String toJson();
}
