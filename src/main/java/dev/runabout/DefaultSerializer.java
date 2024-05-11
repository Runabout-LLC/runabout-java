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

    private static final RunaboutInstance NULL_INSTANCE = RunaboutInstance.of("null", Collections.emptySet());
    private static final RunaboutInstance EMPTY_INSTANCE = RunaboutInstance.of("", Collections.emptySet());

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
    public <T> RunaboutInstance toRunaboutGenericRecursive(final T object, final RunaboutSerializer recursiveSerializer) {

        if (object == null) {
            return NULL_INSTANCE;
        }

        RunaboutInstance instance = null;

        if (object instanceof Map<?,?>) {
            instance = mapSerializer((Map<?,?>) object, recursiveSerializer);
        } else if (object instanceof Collection<?>) {
            instance = collectionSerializer((Collection<?>) object, recursiveSerializer);
        }

        return Optional.ofNullable(instance).orElseGet(() -> toRunaboutGeneric(object));
    }

    //
    // Suppress warnings for unchecked cast to TypedSerializer<T>. We know its safe based on composition of the map.
    //
    @SuppressWarnings("unchecked")
    <T> RunaboutInstance toRunaboutGeneric(final T object) {

        if (object == null) {
            return NULL_INSTANCE;
        }

        RunaboutInstance instance = null;

        if (object instanceof Enum<?>) {
            instance = enumSerializer((Enum<?>) object);
        }

        return Optional.ofNullable(instance)
                .orElseGet(() -> Optional.ofNullable(serializers.get(object.getClass()))
                        .map(serializer -> ((TypedSerializer<T>) serializer).apply(object))
                        .orElse(EMPTY_INSTANCE)
                );
    }

    static RunaboutInstance getNullInput() {
        return NULL_INSTANCE;
    }

    static RunaboutInstance getEmptyInput() {
        return EMPTY_INSTANCE;
    }

    private static RunaboutInstance collectionSerializer(final Collection<?> collection,
                                                         final RunaboutSerializer recursiveSerializer) {

        RunaboutInstance instance = null;

        if (collection instanceof List<?>) {
            instance = listSerializer((List<?>) collection, recursiveSerializer);
        } else if (collection instanceof Set<?>) {
            instance = setSerializer((Set<?>) collection, recursiveSerializer);
        }

        return instance;
    }

    private static RunaboutInstance mapSerializer(final Map<?,?> map, final RunaboutSerializer recursiveSerializer) {

        // Short circuit for empty map
        if (map.isEmpty()) {
            return RunaboutInstance.of("new HashMap<>()", Set.of(HashMap.class.getCanonicalName()));
        }

        final StringBuilder builder = new StringBuilder();
        final Set<String> allDependencies = new HashSet<>();
        allDependencies.add(HashMap.class.getCanonicalName());

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            final RunaboutInstance serialKey = recursiveSerializer.toRunaboutGeneric(entry.getKey());
            final RunaboutInstance serialValue = recursiveSerializer.toRunaboutGeneric(entry.getValue());
            // If either key/value cannot be serialized, return empty instance.
            if (serialKey == null || serialKey.getEval() == null || serialKey.getEval().isEmpty() ||
                    serialValue == null || serialValue.getEval() == null || serialValue.getEval().isEmpty()) {
                return EMPTY_INSTANCE;
            }
            final String entryString = "put(" + serialKey.getEval() + ", " + serialValue.getEval() + "); ";
            builder.append(entryString);
            allDependencies.addAll(serialKey.getDependencies());
            allDependencies.addAll(serialValue.getDependencies());
        }

        return RunaboutInstance.of("new HashMap<>() {{ " + builder + "}}", allDependencies);
    }

    private static RunaboutInstance listSerializer(final List<?> list, final RunaboutSerializer recursiveSerializer) {

        if (list.isEmpty()) {
            return RunaboutInstance.of("new ArrayList<>()", Set.of(ArrayList.class.getCanonicalName()));
        }

        final StringBuilder builder = new StringBuilder();
        final Set<String> allDependencies = new HashSet<>();
        allDependencies.add(ArrayList.class.getCanonicalName());

        for (Object item : list) {
            final RunaboutInstance serialItem = recursiveSerializer.toRunaboutGeneric(item);
            if (serialItem == null || serialItem.getEval() == null || serialItem.getEval().isEmpty()) {
                return EMPTY_INSTANCE;
            }
            builder.append("add(").append(serialItem.getEval()).append("); ");
            allDependencies.addAll(serialItem.getDependencies());
        }

        return RunaboutInstance.of("new ArrayList<>() {{ " + builder + "}}", allDependencies);
    }

    private static RunaboutInstance setSerializer(final Set<?> set, final RunaboutSerializer recursiveSerializer) {

        if (set.isEmpty()) {
            return RunaboutInstance.of("new HashSet<>()", Set.of(HashSet.class.getCanonicalName()));
        }

        final StringBuilder builder = new StringBuilder();
        final Set<String> allDependencies = new HashSet<>();
        allDependencies.add(HashSet.class.getCanonicalName());

        for (Object item : set) {
            final RunaboutInstance serialItem = recursiveSerializer.toRunaboutGeneric(item);
            if (serialItem == null || serialItem.getEval() == null || serialItem.getEval().isEmpty()) {
                return EMPTY_INSTANCE;
            }
            builder.append("add(").append(serialItem.getEval()).append("); ");
            allDependencies.addAll(serialItem.getDependencies());
        }

        return RunaboutInstance.of("new HashSet<>() {{ " + builder + "}}", allDependencies);
    }

    private static RunaboutInstance stringSerializer(final String string) {
        return RunaboutInstance.of("\"" + RunaboutUtils.escapeQuotesOneLayer(string) + "\"", Collections.emptySet());
    }

    private static RunaboutInstance primitiveSerializer(final Object object) {
        final String primitive = object instanceof Integer ? "int" : object.getClass().getSimpleName().toLowerCase();
        return RunaboutInstance.of("(" + primitive + ") " + object, Collections.emptySet());
    }

    static RunaboutInstance charSerializer(final Object object) {
        return RunaboutInstance.of("'" + object + "'", Collections.emptySet());
    }

    private static RunaboutInstance enumSerializer(final Enum<?> e) {
        return RunaboutInstance.of(e.getClass().getCanonicalName() + "." + e.name(),
                Set.of(e.getClass().getCanonicalName()));
    }

    private interface TypedSerializer<T> extends Function<T, RunaboutInstance> {
    }
}
