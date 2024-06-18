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
    public DocumentJsonObject put(String key, Boolean value) {
        document.put(key, value);
        return this;
    }

    @Override
    public DocumentJsonObject put(String key, Number value) {
        document.put(key, value);
        return this;
    }

    @Override
    public DocumentJsonObject put(String key, String value) {
        document.put(key, value);
        return this;
    }

    @Override
    public DocumentJsonObject put(String key, JsonObject value) {
        document.put(key, value);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DocumentJsonObject put(String key, Class<T> clazz, List<T> values) {

        if (clazz == JsonObject.class) {
            final List<Document> documents = ((List<DocumentJsonObject>) values).stream()
                    .map(DocumentJsonObject::getDocument).collect(Collectors.toList());
            document.put(key, documents);
        } else {
            document.put(key, values);
        }

        return this;
    }

    @Override
    public String toJson() {
        return document.toJson();
    }
}
