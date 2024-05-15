package dev.runabout;

import dev.runabout.annotations.ToRunabout;

import java.util.ServiceLoader;

/**
 * Generic serializer interface for Runabout.
 * This interface should be implemented once per project, and the implementation should be a catch-all
 * for any objects that need a custom serializer, but do not contain an instance serializer method in their class.
 * To learn more about how to implement an instance serializer, see {@link ToRunabout}.
 * The implementation should return null if the serializer cannot create a valid RunaboutInput for the given object.
 * For common types, consumers of the API can rely on the default serializer.
 * See {@link DefaultSerializer} for details on which types are serialized out of the box.
 * The implementation should be registered as a service in the META-INF/services/dev.runabout.RunaboutSerializer file.
 */
@FunctionalInterface
public interface RunaboutSerializer {

    static RunaboutSerializer getSerializer() {
        return ServiceLoader.load(RunaboutSerializer.class).findFirst().orElse(null);
    }

    /**
     * Converts an object to a RunaboutInput.
     *
     * @param object The object to serialize.
     * @return A RunaboutInput containing a valid Java statement that can be used to recreate the object.
     */
    RunaboutInput toRunaboutGeneric(final Object object);
}
