package dev.runabout;

import java.util.Set;

/**
 * Interface representing an instance for a Runabout scenario.
 * The Runabout IDE plugin uses String snippets of java expressions to run methods directly.
 * A Runabout instance consists of an eval String and a set of dependencies. The eval String is a java expression
 * which evaluates to a real java instance of an object.
 * The dependencies are the fully qualified class names of all classes used in the eval String.
 */
public interface RunaboutInstance {

    static RunaboutInstance of(final String eval, final Set<String> dependencies) {
        return new RunaboutInstance() {

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
     * Gets the eval String for the Runabout instance, which is a java expression that evaluates to an object.
     *
     * @return The eval String.
     */
    String getEval();

    /**
     * Gets the dependencies for the Runabout instance, which are the fully qualified class names of all
     * classes used in the eval String.
     *
     * @return A set of the dependencies.
     */
    Set<String> getDependencies();
}
