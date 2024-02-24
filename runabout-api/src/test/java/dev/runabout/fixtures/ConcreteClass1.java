package dev.runabout.fixtures;

import java.util.Set;

public class ConcreteClass1 extends AbstractClass1 {

    public ConcreteClass1(String name, Set<String> keys) {
        super(name, keys);
    }

    @Override
    public void putKey(String key) {
        keys.add(key);
    }

    @Override
    public void clearKeys() {
        keys.clear();
    }
}
