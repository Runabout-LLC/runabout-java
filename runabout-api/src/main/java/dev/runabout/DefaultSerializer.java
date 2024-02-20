package dev.runabout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

class DefaultSerializer {

    private static final Map<Class<?>, TypedSerializer<?>> serializers = Map.ofEntries(
            Map.entry(String.class, (TypedSerializer<String>) DefaultSerializer::stringSerializer),
            Map.entry(Boolean.class, (TypedSerializer<Boolean>) DefaultSerializer::primitiveSerializer),
            Map.entry(Integer.class,(TypedSerializer<Integer>) DefaultSerializer::primitiveSerializer),
            Map.entry(Long.class,(TypedSerializer<Long>) DefaultSerializer::primitiveSerializer),
            Map.entry(Float.class,(TypedSerializer<Float>) DefaultSerializer::primitiveSerializer),
            Map.entry(Double.class,(TypedSerializer<Double>) DefaultSerializer::primitiveSerializer),
            Map.entry(Byte.class,(TypedSerializer<Byte>) DefaultSerializer::primitiveSerializer),
            Map.entry(Short.class,(TypedSerializer<Short>) DefaultSerializer::primitiveSerializer),
            Map.entry(Character.class, (TypedSerializer<Character>) DefaultSerializer::charSerializer)
    );

    private static final DefaultSerializer INSTANCE = new DefaultSerializer();

    public static DefaultSerializer getInstance() {
        return INSTANCE;
    }

    private DefaultSerializer() {
        // Singleton.
    }

    public <T> RunaboutInput toRunaboutGenericRecursive(final T object, final RunaboutSerializer recursiveSerializer) {

        RunaboutInput input = null;

        if (object instanceof Map<?,?>) {
            input = mapSerializer((Map<?,?>) object, recursiveSerializer);
        } else if (object instanceof Collection<?>) {
            input = collectionSerializer((Collection<?>) object, recursiveSerializer);
        }

        return Optional.ofNullable(input).orElseGet(() -> toRunaboutGeneric(object));
    }

    public <T> RunaboutInput toRunaboutGeneric(final T object) {

        RunaboutInput input = null;

        if (object instanceof Enum<?>) {
            input = enumSerializer((Enum<?>) object);
        }

        return Optional.ofNullable(input)
                .orElseGet(() -> Optional.ofNullable(serializers.get(object.getClass()))
                        .map(serializer -> ((TypedSerializer<T>) serializer).apply(object))
                        .orElseGet(() -> RunaboutInput.of("", Collections.emptySet())));
    }

    private static RunaboutInput collectionSerializer(final Collection<?> collection,
                                                      final RunaboutSerializer recursiveSerializer) {

        RunaboutInput input = null;

        if (collection instanceof List<?>) {
            input = listSerializer((List<?>) collection, recursiveSerializer);
        } else if (collection instanceof Set<?>) {
            input = setSerializer((Set<?>) collection, recursiveSerializer);
        }

        return input;
    }

    private static RunaboutInput mapSerializer(final Map<?,?> map, final RunaboutSerializer recursiveSerializer) {

        // Short circuit for empty map
        if (map.isEmpty()) {
            return RunaboutInput.of("new HashMap<>()", Set.of(HashMap.class.getCanonicalName()));
        }

        final StringJoiner joiner = new StringJoiner(", ");
        final Set<String> allDependencies = new HashSet<>();
        allDependencies.add(Map.class.getCanonicalName());
        allDependencies.add(HashMap.class.getCanonicalName());

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                final RunaboutInput serialKey = recursiveSerializer.toRunaboutGeneric(entry.getKey());
                final RunaboutInput serialValue = recursiveSerializer.toRunaboutGeneric(entry.getValue());
                if (serialKey == null || serialKey.getEval() == null || serialKey.getEval().isEmpty() ||
                        serialValue == null || serialValue.getEval() == null || serialValue.getEval().isEmpty()) {
                    return RunaboutInput.of("", Collections.emptyList());
                }
                final String entryString = "Map.entry(" + serialKey.getKey() + ", " + serialValue.getKey() + ")";
                joiner.add(entryString);
                allDependencies.addAll(serialKey.getValue());
                allDependencies.addAll(serialValue.getValue());
            }
        }

        return RunaboutInput.of("new HashMap<>(Map.ofEntries(" + joiner + "))", allDependencies);
    }

    private static RunaboutInput listSerializer(final List<?> list, final RunaboutSerializer recursiveSerializer) {

        if (list.isEmpty()) {
            return RunaboutInput.of("new ArrayList<>()", Set.of(ArrayList.class.getCanonicalName()));
        }

        final StringJoiner joiner = new StringJoiner(", ");
        final Set<String> allDependencies = new HashSet<>();
        allDependencies.add(List.class.getCanonicalName());
        allDependencies.add(ArrayList.class.getCanonicalName());

        for (Object item : list) {
            if (item != null) {
                final RunaboutInput serialItem = recursiveSerializer.toRunaboutGeneric(item);
                if (serialItem == null || serialItem.getKey() == null || serialItem.getKey().isEmpty()) {
                    return RunaboutInput.of("", Collections.emptyList());
                }
                joiner.add(serialItem.getKey());
                allDependencies.addAll(serialItem.getValue());
            }
        }

        return RunaboutInput.of("new ArrayList<>(List.of(" + joiner + "))", allDependencies);
    }

    private static RunaboutInput setSerializer(final Set<?> set, final RunaboutSerializer recursiveSerializer) {

        if (set.isEmpty()) {
            return RunaboutInput.of("new HashSet<>()", Set.of(HashSet.class.getCanonicalName()));
        }

        final StringJoiner joiner = new StringJoiner(", ");
        final Set<String> allDependencies = new HashSet<>();
        allDependencies.add(Set.class.getCanonicalName());
        allDependencies.add(HashSet.class.getCanonicalName());

        for (Object item : set) {
            if (item != null) {
                final RunaboutInput serialItem = recursiveSerializer.toRunaboutGeneric(item);
                if (serialItem == null || serialItem.getKey() == null || serialItem.getKey().isEmpty()) {
                    return RunaboutInput.of("", Collections.emptyList());
                }
                joiner.add(serialItem.getKey());
                allDependencies.addAll(serialItem.getValue());
            }
        }

        return RunaboutInput.of("new HashSet<>(Set.of(" + joiner + "))", allDependencies);
    }

    private static RunaboutInput stringSerializer(final String string) {
        return RunaboutInput.of("\"" + RunaboutUtils.escapeQuotesOneLayer(string) + "\"", Collections.emptySet());
    }

    private static RunaboutInput primitiveSerializer(final Object object) {
        final String primitive = object instanceof Integer ? "int" : object.getClass().getSimpleName().toLowerCase();
        return RunaboutInput.of("(" + primitive + ") " + object, Collections.emptySet());
    }

    static RunaboutInput charSerializer(final Object object) {
        return RunaboutInput.of("'" + object + "'", Collections.emptySet());
    }

    private static RunaboutInput enumSerializer(final Enum<?> e) {
        return RunaboutInput.of(e.getClass().getCanonicalName() + "." + e.name(),
                Set.of(e.getClass().getCanonicalName()));
    }

    private interface TypedSerializer<T> extends Function<T, RunaboutInput> {
    }
}
