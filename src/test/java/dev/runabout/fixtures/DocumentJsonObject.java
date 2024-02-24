package dev.runabout.fixtures;

import dev.runabout.JsonObject;
import org.bson.Document;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentJsonObject implements JsonObject {

    private final Document document;

    public DocumentJsonObject() {
        this.document = new Document();
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public void put(String key, Boolean value) {
        document.put(key, value);
    }

    @Override
    public void put(String key, Number value) {
        document.put(key, value);
    }

    @Override
    public void put(String key, String value) {
        document.put(key, value);
    }

    @Override
    public void put(String key, JsonObject value) {
        document.put(key, value);
    }

    @Override
    public <T> void put(String key, Class<T> clazz, List<T> values) {

        if (clazz == JsonObject.class) {
            final List<Document> documents = ((List<DocumentJsonObject>) values).stream()
                    .map(DocumentJsonObject::getDocument).collect(Collectors.toList());
            document.put(key, documents);
        } else {
            document.put(key, values);
        }
    }

    @Override
    public String toJson() {
        return document.toJson();
    }
}
