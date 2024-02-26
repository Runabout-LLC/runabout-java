package dev.runabout.fixtures;

import dev.runabout.RunaboutInput;
import dev.runabout.RunaboutService;
import dev.runabout.ToRunabout;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractClass1 {

    protected final String name;
    protected final Set<String> keys;

    public AbstractClass1(final String name, final Set<String> keys) {
        this.name = name;
        this.keys = keys;
    }

    public String getName() {
        return name;
    }

    public Set<String> getKeys() {
        return keys;
    }

    public abstract void putKey(String key);

    public abstract void clearKeys();

    @ToRunabout
    private RunaboutInput toRunabout() {
        final RunaboutService<?> runaboutService = RunaboutService.getService();
        final RunaboutInput nameInput = runaboutService.serialize(name);
        final RunaboutInput keysInput = runaboutService.serialize(keys);
        final String className = this.getClass().getSimpleName();
        final String eval = String.format("new %1$s(%2$s, %3$s)", className, nameInput.getEval(), keysInput.getEval());
        final Set<String> dependencies = new HashSet<>(nameInput.getDependencies());
        dependencies.addAll(keysInput.getDependencies());
        dependencies.add(this.getClass().getCanonicalName());
        return RunaboutInput.of(eval, dependencies);
    }
}
