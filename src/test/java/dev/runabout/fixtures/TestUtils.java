package dev.runabout.fixtures;

import dev.runabout.RunaboutService;
import dev.runabout.RunaboutServiceBuilder;

public class TestUtils {

    public static RunaboutService<DocumentJsonObject> getDocumentRunaboutService() {
        return new RunaboutServiceBuilder<>(DocumentJsonObject::new).build();
    }
}
