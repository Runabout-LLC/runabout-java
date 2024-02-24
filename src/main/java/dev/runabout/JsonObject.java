package dev.runabout;

import java.util.List;

/**
 * A lightweight interface for dealing with JSON objects.
 */
public interface JsonObject {

    /**
     * Puts a boolean value into the JSON object.
     *
     * @param key   the key to associate with the value.
     * @param value the value to put into the JSON object.
     */
    void put(String key, Boolean value);

    /**
     * Puts a number value into the JSON object.
     *
     * @param key   the key to associate with the value.
     * @param value the value to put into the JSON object.
     */
    void put(String key, Number value);


    /**
     * Puts a string value into the JSON object.
     *
     * @param key   the key to associate with the value.
     * @param value the value to put into the JSON object.
     */
    void put(String key, String value);

    /**
     * Puts a JSON object value into the JSON object.
     *
     * @param key   the key to associate with the value.
     * @param value the value to put into the JSON object.
     */
    void put(String key, JsonObject value);

    /**
     * Puts a list of values into the JSON object.
     *
     * @param key    the key to associate with the value.
     * @param clazz  the class of the values.
     * @param values the values to put into the JSON object.
     * @param <T>    the type of the values.
     */
    <T> void put(String key, Class<T> clazz, List<T> values);

    /**
     * Converts the JSON object to a JSON string.
     *
     * @return the JSON string.
     */
    String toJson();
}
