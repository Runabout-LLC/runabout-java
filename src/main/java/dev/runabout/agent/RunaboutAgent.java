package dev.runabout.agent;

import java.util.Set;

public interface RunaboutAgent {

    void install();

    void enable();

    void refresh();

    void refresh(Set<Instruction> instructions);

    void disable();
}
