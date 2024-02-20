package dev.runabout.json;

public interface JsonFactory<T extends JsonObject> {

    T createObject();
}
