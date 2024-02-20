package dev.runabout;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for reading property files.
 */
class RunaboutProperties {

    private static final String PROPERTIES_FILE = "runabout.properties";
    private static RunaboutProperties INSTANCE;

    private final String JSON_CONTRACT_VERSION;

    private RunaboutProperties() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final Properties properties = new Properties();
        try (InputStream inputStream = loader.getResourceAsStream(PROPERTIES_FILE)) {
            properties.load(inputStream);
            JSON_CONTRACT_VERSION = properties.getProperty("JSON_CONTRACT_VERSION");
        } catch (Exception e) {
            throw new RuntimeException("Could not load properties file: " + PROPERTIES_FILE, e);
        }
    }

    public String getJsonContractVersion() {
        return JSON_CONTRACT_VERSION;
    }

    static synchronized RunaboutProperties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RunaboutProperties();
        }
        return INSTANCE;
    }
}
