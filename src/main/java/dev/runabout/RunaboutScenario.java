package dev.runabout;

import dev.runabout.annotations.Nullable;
import dev.runabout.utils.RunaboutConstants;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class RunaboutScenario {

    private final String method;
    private final String eventId;
    private final String project;
    private final Timestamp datetime;
    private final JsonObject properties;
    private final Collection<RunaboutInstance> instances;


    public RunaboutScenario(String method, String eventId, @Nullable String project, Timestamp datetime,
                            @Nullable JsonObject properties, Collection<RunaboutInstance> instances) {
        this.method = method;
        this.eventId = eventId;
        this.project = project;
        this.datetime = datetime;
        this.properties = properties;
        this.instances = instances;
    }

    public String getMethod() {
        return method;
    }

    public String getEventId() {
        return eventId;
    }

    public String getProject() {
        return project;
    }

    public Timestamp getDatetime() {
        return datetime;
    }

    public JsonObject getProperties() {
        return properties;
    }

    public Collection<RunaboutInstance> getInstances() {
        return instances;
    }

    public JsonObject toJsonObject() {
        return toJsonObject(JsonObjectImpl::new);
    }

    public JsonObject toJsonObject(final Supplier<JsonObject> jsonFactory) {
        final List<JsonObject> jsonInstances = instances.stream()
                .map(instance -> instance.toJsonObject(jsonFactory))
                .collect(Collectors.toList());
        return jsonFactory.get()
                .put(RunaboutConstants.VERSION_KEY, RunaboutConstants.JSON_CONTRACT_VERSION)
                .put(RunaboutConstants.EVENT_ID_KEY, eventId)
                .put(RunaboutConstants.PROJECT_KEY, project)
                .put(RunaboutConstants.DATETIME_KEY, datetime.toString())
                .put(RunaboutConstants.PROPERTIES_KEY, properties)
                .put(RunaboutConstants.METHOD_KEY, method)
                .put(RunaboutConstants.INSTANCES_KEY, JsonObject.class, jsonInstances);
    }
}
