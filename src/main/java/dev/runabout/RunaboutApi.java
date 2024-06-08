package dev.runabout;

/**
 * Runabout interface representing Runabout API endpoints relevant to the library.
 * The library comes with a default implementation and a builder to meet most use cases.
 * See {@link RunaboutApiBuilder} for more information.
 */
public interface RunaboutApi {

    /**
     * Saves a scenario via the Runabout Ingest API.
     *
     * @param scenario The scenario to save.
     */
    void ingestScenario(final RunaboutScenario scenario);
}
