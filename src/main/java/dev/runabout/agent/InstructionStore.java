package dev.runabout.agent;

import dev.runabout.RunaboutListener;
import dev.runabout.RunaboutUtils;
import dev.runabout.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

class InstructionStore {

    private final Map<Method, Long> methodMap = new HashMap<>();
    private final Map<Class<?>, Long> classMap = new HashMap<>();
    private final Set<Instruction> instructions = new HashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    @Nullable
    public Long getTimeout(final Class<?> clazz, final Method method) {
        try {
            lock.readLock().lock();
            return Optional.ofNullable(methodMap.get(method)).orElseGet(() -> classMap.get(clazz));
        } finally {
            lock.readLock().unlock();
        }
    }

    void update(final Set<Instruction> instructions,
                final BiConsumer<Class<?>, Method> installInstructions,
                final BiConsumer<Class<?>, Method> removeInstructions,
                final RunaboutListener listener) {

        final Set<Instruction> prunedInstructions = prune(instructions);

        //
        // Invoke remove callback for instructions that are no longer in the set.
        //
        final Set<Instruction> toRemove = new HashSet<>(this.instructions);
        toRemove.removeAll(prunedInstructions);
        toRemove.stream()
                .map(InstructionStore::readReferences)
                .forEach(p -> installInstructions.accept(p.left, p.right));

        //
        // Invoke install callback for instructions that are new to the set.
        //
        final Set<Instruction> toInstall = new HashSet<>(prunedInstructions);
        toInstall.removeAll(this.instructions);
        toInstall.stream()
                .map(InstructionStore::readReferences)
                .forEach(p -> installInstructions.accept(p.left, p.right));

        try {
            lock.writeLock().lock();

            //
            // Replace instructions.
            //
            this.instructions.clear();
            this.instructions.addAll(prunedInstructions);

            //
            // Update timeout maps.
            //
            classMap.clear();
            methodMap.clear();
            this.instructions.forEach(instruction -> cacheInstruction(instruction, listener));
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void cacheInstruction(final Instruction instruction, final RunaboutListener listener) {
        final long timeout = instruction.getTimeout();
        final String reference = instruction.getReference();

        try {
            switch (instruction.getReferenceType()) {
                case METHOD:
                    methodMap.put(RunaboutUtils.runaboutStringToMethod(reference), timeout);
                    break;
                case CLASS:
                    classMap.put(RunaboutUtils.getClass(reference), timeout);
                    break;
            }
        } catch (final Exception e) {
            listener.onError(e);
        }
    }

    private static Pair<Class<?>, Method> readReferences(final Instruction instruction) {
        final String reference = instruction.getReference();
        switch (instruction.getReferenceType()) {
            case METHOD:
                final Method method = RunaboutUtils.runaboutStringToMethod(reference);
                return new Pair<>(method.getDeclaringClass(), method);
            case CLASS:
                return new Pair<>(RunaboutUtils.getClass(reference), null);
        }
        throw new IllegalArgumentException("Invalid reference type: " + instruction.getReferenceType());
    }

    //
    // Prune instructions that have timeouts in the future.
    // Remove duplicates on a given reference by keeping the one with the latest timeout.
    //
    private static Set<Instruction> prune(Set<Instruction> instructions) {
        final Map<String, Instruction> instructionMap = instructions.stream()
                .filter(instruction -> instruction.getTimeout() > System.currentTimeMillis())
                .collect(Collectors.toMap(Instruction::getReferenceURI, i -> i,
                        (a, b) -> a.getTimeout() > b.getTimeout() ? a : b));
        return new HashSet<>(instructionMap.values());
    }

    private static final class Pair<T,U> {

        final T left;
        final U right;

        private Pair(T left, U right) {
            this.left = left;
            this.right = right;
        }
    }
}
