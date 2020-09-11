package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * An optional helper class for deserializing a digital twin
 */
@Fluent
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ComponentMetadata {

    /**
     * Model-defined writable properties' request state.
     */
    private final Map<String, Object> writeableProperties = new HashMap<>();

    public ComponentMetadata() {}

    /**
     * Gets the model-defined writable properties' request state.
     * For your convenience, the value of each map can be turned into an instance of {@link WritableProperty}.
     * @return The model-defined writable properties' request state.
     */
    @JsonAnyGetter
    public Map<String, Object> getWriteableProperties() {
        return writeableProperties;
    }

    /**
     * Sets the model-defined writable properties' request state.
     * @return The ComponentMetadata object itself.
     */
    @JsonAnySetter
    ComponentMetadata setWritableProperties(String key, Object value) {
        this.writeableProperties.put(key, value);
        return this;
    }
}
