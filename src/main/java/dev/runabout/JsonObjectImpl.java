package dev.runabout;

import dev.runabout.utils.RunaboutUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;

class JsonObjectImpl extends HashMap<String, Object> implements JsonObject {

    @Override
    public JsonObject put(String key, Boolean value) {
        super.put(key, value);
        return this;
    }

    @Override
    public JsonObject put(String key, Number value) {
        super.put(key, value);
        return this;
    }

    @Override
    public JsonObject put(String key, String value) {
        super.put(key, value);
        return this;
    }

    @Override
    public JsonObject put(String key, JsonObject value) {
        super.put(key, value);
        return this;
    }

    @Override
    public <T> JsonObject put(String key, Class<T> clazz, List<T> values) {

        if (clazz != String.class && clazz != Boolean.class &&
                doesNotImplement(clazz, Number.class) &&
                doesNotImplement(clazz, JsonObject.class)) {
            throw new RunaboutException(String.format("Unknown list type for Runabout JsonObject: [%1$s]",
                    clazz.getCanonicalName()));
        }

        super.put(key, values);
        return this;
    }

    private boolean doesNotImplement(final Class<?> clazz, final Class<?> interfaceClass) {
        return clazz != interfaceClass && !Set.of(clazz.getInterfaces()).contains(interfaceClass);
    }

    @Override
    public String toJson() {

        final StringJoiner commaJoiner = new StringJoiner(",");

        for (final Map.Entry<String, Object> entry: entrySet()) {

            final StringBuilder singleKvp = new StringBuilder()
                    .append("\"")
                    .append(RunaboutUtils.escapeQuotesOneLayer(entry.getKey()))
                    .append("\":")
                    .append(valueToString(entry.getValue()));

            commaJoiner.add(singleKvp);
        }

        return "{" + commaJoiner + "}";
    }

    private static String valueToString(final Object value) {

        String string;

        if (value instanceof JsonObject) {
            string = ((JsonObject) value).toJson();
        } else if (value instanceof String) {
            string = "\"" + RunaboutUtils.escapeQuotesOneLayer((String) value) + "\"";
        } else if (value instanceof List<?>) {
            final StringJoiner commaJoiner = new StringJoiner(",");
            ((List<?>) value).forEach(innerValue -> commaJoiner.add(valueToString(innerValue)));
            string = "[" + commaJoiner + "]";
        } else {
            string = Optional.ofNullable(value).map(Object::toString).orElse("null");
        }

        return string;
    }

    static class JsonFactoryImpl implements Supplier<JsonObject> {

        @Override
        public JsonObjectImpl get() {
            return new JsonObjectImpl();
        }
    }
}
