package dev.runabout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

class DefaultRunaboutSerializer {

    private static final Map<Class<?>, RecursiveSerializer<?>> serializers = Map.ofEntries(
            Map.entry(Map.class, (RecursiveSerializer<Map<?,?>>) DefaultRunaboutSerializer::mapSerializer),
            Map.entry(List.class, (RecursiveSerializer<List<?>>) DefaultRunaboutSerializer::listSerializer),
            Map.entry(Set.class, (RecursiveSerializer<Set<?>>) DefaultRunaboutSerializer::setSerializer),
            Map.entry(String.class, (NonRecursiveSerializer<String>) DefaultRunaboutSerializer::stringSerializer),
            Map.entry(Boolean.class, (NonRecursiveSerializer<Boolean>) DefaultRunaboutSerializer::primitiveSerializer),
            Map.entry(Integer.class,(NonRecursiveSerializer<Integer>) DefaultRunaboutSerializer::primitiveSerializer),
            Map.entry(Long.class,(NonRecursiveSerializer<Long>) DefaultRunaboutSerializer::primitiveSerializer),
            Map.entry(Float.class,(NonRecursiveSerializer<Float>) DefaultRunaboutSerializer::primitiveSerializer),
            Map.entry(Double.class,(NonRecursiveSerializer<Double>) DefaultRunaboutSerializer::primitiveSerializer),
            Map.entry(Byte.class,(NonRecursiveSerializer<Byte>) DefaultRunaboutSerializer::primitiveSerializer),
            Map.entry(Short.class,(NonRecursiveSerializer<Short>) DefaultRunaboutSerializer::primitiveSerializer),
            Map.entry(Character.class, (NonRecursiveSerializer<Character>) DefaultRunaboutSerializer::charSerializer)
    );

    private static final DefaultRunaboutSerializer INSTANCE = new DefaultRunaboutSerializer();

    public static DefaultRunaboutSerializer getInstance() {
        return INSTANCE;
    }

    private DefaultRunaboutSerializer() {
        // Singleton.
    }

    public <T> RunaboutInput toRunaboutGeneric(final T object, final RunaboutSerializer recursiveSerializer) {

        RunaboutInput input;

        if (object instanceof Enum<?>) {
            input = enumSerializer((Enum<?>) object);
        } else {
            input = Optional.ofNullable(serializers.get(object.getClass()))
                    .map(serializer -> ((RecursiveSerializer<T>) serializer)
                            .toRunabout(object, recursiveSerializer))
                    .orElse(null);
        }

        return Optional.ofNullable(input).orElseGet(() -> RunaboutInput.of("", Collections.emptySet()));
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

    private interface RecursiveSerializer<T> {
        RunaboutInput toRunabout(final T object, final RunaboutSerializer recursiveSerializer);
    }

    private interface NonRecursiveSerializer<T> extends RecursiveSerializer<T> {

        RunaboutInput toRunabout(final T object);

        default RunaboutInput toRunabout(final T object, final RunaboutSerializer recursiveSerializer) {
            return toRunabout(object);
        }
    }
}
