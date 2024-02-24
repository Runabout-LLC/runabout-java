package dev.runabout.fixtures;

import java.util.Map;

public class ConcreteClass2 {

    private final int id;
    private final Map<String, String> data;

    public ConcreteClass2(int id, Map<String, String> data) {
        this.id = id;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public Map<String, String> getData() {
        return data;
    }
}
