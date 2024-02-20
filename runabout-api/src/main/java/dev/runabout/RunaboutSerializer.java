package dev.runabout;

import java.util.ServiceLoader;

@FunctionalInterface
public interface RunaboutSerializer {

    static RunaboutSerializer getSerializer() {
        return ServiceLoader.load(RunaboutSerializer.class).findFirst().orElse(null);
    }

    RunaboutInput toRunaboutGeneric(final Object object);
}
