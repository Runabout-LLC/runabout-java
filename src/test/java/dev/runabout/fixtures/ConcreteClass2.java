package dev.runabout.fixtures;

import dev.runabout.RunaboutEnabled;
import dev.runabout.RunaboutParameter;

import java.util.Map;

public class ConcreteClass2 {

    private final int id;
    private final Map<String, String> data;

    @RunaboutEnabled
    public ConcreteClass2(@RunaboutParameter("id") int id, @RunaboutParameter("data") Map<String, String> data) {
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
