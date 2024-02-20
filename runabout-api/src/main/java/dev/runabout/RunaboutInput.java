package dev.runabout;

import java.util.Set;

public interface RunaboutInput {

    static RunaboutInput of(final String eval, final Set<String> dependencies) {
        return new RunaboutInput() {

            @Override
            public String getEval() {
                return eval;
            }

            @Override
            public Set<String> getDependencies() {
                return dependencies;
            }
        };
    }

    String getEval();

    Set<String> getDependencies();
}
