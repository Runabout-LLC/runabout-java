package dev.runabout.agent;

import java.util.Set;

public interface RunaboutAgent {

    void install();

    void refresh();

    void refresh(Set<Command> commands);

    void disable();
}
