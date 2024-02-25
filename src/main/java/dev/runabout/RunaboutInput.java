package dev.runabout;

import java.util.Set;

/**
 * Interface representing an input for a Runabout scenario.
 * The Runabout IDE plugin uses String snippets of java expressions to run methods directly.
 * A Runabout input consists of an eval String and a set of dependencies. The eval String is a java expression
 * which evaluates to an object. The dependencies are the classes used in the eval String.
 */
public interface RunaboutInput {

    static RunaboutInput of(final String eval, final Set<Class<?>> dependencies) {
        return new RunaboutInput() {

            @Override
            public String getEval() {
                return eval;
            }

            @Override
            public Set<Class<?>> getDependencies() {
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
     * Gets all dependent classes for the Runabout Input; All classes used in the eval String.
     *
     * @return A set of the dependent classes.
     */
    Set<Class<?>> getDependencies();
}
