package dev.runabout;

import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;

/**
 * POJO representing the json structure of a single object as an input for a Runabout scenario.
 */
public class RunaboutInstance implements RunaboutInput {

    private final String type;
    private final String eval;
    private final Set<String> dependencies;

    public RunaboutInstance(String type, String eval, Set<String> dependencies) {
        this.type = type;
        this.eval = eval;
        this.dependencies = dependencies;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getEval() {
        return eval;
    }

    @Override
    public Set<String> getDependencies() {
        return dependencies;
    }

    public JsonObject toJsonObject() {
        return toJsonObject(JsonObjectImpl::new);
    }

    public JsonObject toJsonObject(final Supplier<JsonObject> jsonFactory) {
        return jsonFactory.get()
                .put(RunaboutConstants.TYPE_KEY, type)
                .put(RunaboutConstants.EVAL_KEY, eval)
                .put(RunaboutConstants.DEPENDENCIES_KEY, String.class, new ArrayList<>(dependencies));
    }

    public static RunaboutInstance of(final String type, final RunaboutInput input) {
        return new RunaboutInstance(type, input.getEval(), input.getDependencies());
    }
}
