package dev.runabout;

import java.util.Map;

class TypeSafeMap<K, V> {

    private final Map<K, V> map;

    TypeSafeMap(Map<K, V> map) {
        this.map = map;
    }

    public <T> V<T> get(K<T> key) {
        return (Value) map.get(key);
        // we know it's safe, but the compiler can't prove it
    }
}
