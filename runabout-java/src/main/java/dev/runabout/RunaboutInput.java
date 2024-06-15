package dev.runabout;

import java.util.Set;

/**
 * Interface representing a single object as an input for a Runabout scenario.
 * The Runabout IDE plugin uses String snippets of java expressions to run methods directly.
 * A Runabout input consists of an eval String and a set of dependencies. The eval String is a java expression
 * which evaluates to an object. The dependencies are the fully qualified class names of all classes used in
 * the eval String.
 */
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

    /**
     * Gets the eval String for the Runabout input, which is a java expression that evaluates to an object.
     *
     * @return The eval String.
     */
    String getEval();

    /**
     * Gets the dependencies for the Runabout input, which are the fully qualified class names of all
     * classes used in the eval String.
     *
     * @return A set of the dependencies.
     */
    Set<String> getDependencies();
}
