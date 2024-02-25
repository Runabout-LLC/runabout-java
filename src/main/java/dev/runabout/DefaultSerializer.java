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

    private static final RunaboutInput NULL_INPUT = RunaboutInput.of("null", Collections.emptySet());
    private static final RunaboutInput EMPTY_INPUT = RunaboutInput.of("", Collections.emptySet());

    private static final DefaultSerializer INSTANCE = new DefaultSerializer();

    public static DefaultSerializer getInstance() {
        return INSTANCE;
    }

    private DefaultSerializer() {
        // Singleton.
    }

    /**
     * Main entrypoint for serializing objects.
     */
    public <T> RunaboutInput toRunaboutGenericRecursive(final T object, final RunaboutSerializer recursiveSerializer) {

        if (object == null) {
            return NULL_INPUT;
        }

        RunaboutInput input = null;

        if (object instanceof Map<?,?>) {
            input = mapSerializer((Map<?,?>) object, recursiveSerializer);
        } else if (object instanceof Collection<?>) {
            input = collectionSerializer((Collection<?>) object, recursiveSerializer);
        }

        return Optional.ofNullable(input).orElseGet(() -> toRunaboutGeneric(object));
    }

    //
    // Suppress warnings for unchecked cast to TypedSerializer<T>. We know its safe based on composition of the map.
    //
    @SuppressWarnings("unchecked")
    <T> RunaboutInput toRunaboutGeneric(final T object) {

        if (object == null) {
            return NULL_INPUT;
        }

        RunaboutInput input = null;

        if (object instanceof Enum<?>) {
            input = enumSerializer((Enum<?>) object);
        }

        return Optional.ofNullable(input)
                .orElseGet(() -> Optional.ofNullable(serializers.get(object.getClass()))
                        .map(serializer -> ((TypedSerializer<T>) serializer).apply(object))
                        .orElse(EMPTY_INPUT)
                );
    }

    static RunaboutInput getNullInput() {
        return NULL_INPUT;
    }

    static RunaboutInput getEmptyInput() {
        return EMPTY_INPUT;
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
            return RunaboutInput.of("new HashMap<>()", Set.of(HashMap.class));
        }

        final StringBuilder builder = new StringBuilder();
        final Set<Class<?>> allDependencies = new HashSet<>();
        allDependencies.add(HashMap.class);

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            final RunaboutInput serialKey = recursiveSerializer.toRunaboutGeneric(entry.getKey());
            final RunaboutInput serialValue = recursiveSerializer.toRunaboutGeneric(entry.getValue());
            // If either key/value cannot be serialized, return empty input.
            if (serialKey == null || serialKey.getEval() == null || serialKey.getEval().isEmpty() ||
                    serialValue == null || serialValue.getEval() == null || serialValue.getEval().isEmpty()) {
                return RunaboutInput.of("", Collections.emptySet());
            }
            final String entryString = "put(" + serialKey.getEval() + ", " + serialValue.getEval() + "); ";
            builder.append(entryString);
            allDependencies.addAll(serialKey.getDependencies());
            allDependencies.addAll(serialValue.getDependencies());
        }

        return RunaboutInput.of("new HashMap<>() {{ " + builder + "}}", allDependencies);
    }

    private static RunaboutInput listSerializer(final List<?> list, final RunaboutSerializer recursiveSerializer) {

        if (list.isEmpty()) {
            return RunaboutInput.of("new ArrayList<>()", Set.of(ArrayList.class));
        }

        final StringBuilder builder = new StringBuilder();
        final Set<Class<?>> allDependencies = new HashSet<>();
        allDependencies.add(ArrayList.class);

        for (Object item : list) {
            final RunaboutInput serialItem = recursiveSerializer.toRunaboutGeneric(item);
            if (serialItem == null || serialItem.getEval() == null || serialItem.getEval().isEmpty()) {
                return EMPTY_INPUT;
            }
            builder.append("add(").append(serialItem.getEval()).append("); ");
            allDependencies.addAll(serialItem.getDependencies());
        }

        return RunaboutInput.of("new ArrayList<>() {{ " + builder + "}}", allDependencies);
    }

    private static RunaboutInput setSerializer(final Set<?> set, final RunaboutSerializer recursiveSerializer) {

        if (set.isEmpty()) {
            return RunaboutInput.of("new HashSet<>()", Set.of(HashSet.class));
        }

        final StringBuilder builder = new StringBuilder();
        final Set<Class<?>> allDependencies = new HashSet<>();
        allDependencies.add(HashSet.class);

        for (Object item : set) {
            final RunaboutInput serialItem = recursiveSerializer.toRunaboutGeneric(item);
            if (serialItem == null || serialItem.getEval() == null || serialItem.getEval().isEmpty()) {
                return EMPTY_INPUT;
            }
            builder.append("add(").append(serialItem.getEval()).append("); ");
            allDependencies.addAll(serialItem.getDependencies());
        }

        return RunaboutInput.of("new HashSet<>() {{ " + builder + "}}", allDependencies);
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
                Set.of(e.getClass()));
    }

    private interface TypedSerializer<T> extends Function<T, RunaboutInput> {
    }
}
