package dev.runabout.agent;

import dev.runabout.RunaboutListener;
import dev.runabout.RunaboutUtils;
import dev.runabout.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CommandStore {

    private final Map<Method, Long> methodMap = new HashMap<>();
    private final Map<Class<?>, Long> classMap = new HashMap<>();

    private void put(final Class<?> clazz, final long timeout) {
        classMap.put(clazz, timeout);
    }

    private void put(final Method method, final long timeout) {
        methodMap.put(method, timeout);
    }

    @Nullable
    public Long get(final Class<?> clazz, final Method method) {
        return Optional.ofNullable(methodMap.get(method)).orElseGet(() -> classMap.get(clazz));
    }

    static CommandStore newCommandStore(final Collection<Command> commands, final RunaboutListener listener) {
        final CommandStore commandStore = new CommandStore();
        commands.forEach(command -> {

            final long timeout = command.getTimeout();
            final String reference = command.getReference();

            try {
                switch (command.getReferenceType()) {
                    case METHOD:
                        commandStore.put(RunaboutUtils.runaboutStringToMethod(reference), timeout);
                        break;
                    case CLASS:
                        commandStore.put(RunaboutUtils.getClass(reference), timeout);
                        break;
                }
            } catch (final Exception e) {
                listener.onError(e);
            }
        });
        return commandStore;
    }
}
