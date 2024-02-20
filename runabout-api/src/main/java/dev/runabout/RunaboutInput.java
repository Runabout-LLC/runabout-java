package dev.runabout;

import java.util.Collection;
import java.util.Map;

public interface RunaboutInput extends Map.Entry<String, Collection<String>>{

    static RunaboutInput of(final String eval, final Collection<String> dependencies) {
        return new RunaboutInput() {

            private Collection<String> dependenciesInternal = dependencies;

            @Override
            public String getKey() {
                return eval;
            }

            @Override
            public Collection<String> getValue() {
                return dependenciesInternal;
            }

            @Override
            public Collection<String> setValue(Collection<String> value) {
                dependenciesInternal = value;
                return dependenciesInternal;
            }
        };
    }

    static RunaboutInput of(final Map.Entry<String, Collection<String>> entry) {
        return RunaboutInput.of(entry.getKey(), entry.getValue());
    }

    default String getEval() {
        return getKey();
    }

    default Collection<String> getDependencies() {
        return getValue();
    }
}
