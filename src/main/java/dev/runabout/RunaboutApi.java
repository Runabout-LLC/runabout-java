package dev.runabout;

import dev.runabout.agent.Instruction;

import java.util.List;
import java.util.Set;

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

    /**
     * TODO javadocs
     * @param project
     * @return
     */
    Set<Instruction> getLatestInstructions(final String project);
}
