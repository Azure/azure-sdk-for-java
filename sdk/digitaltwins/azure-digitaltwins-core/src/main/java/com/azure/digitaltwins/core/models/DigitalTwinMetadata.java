// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

/**
 * An optional, helper class for deserializing a digital twin.
 * The $metadata class on a {@link BasicDigitalTwin}.
 * Only properties with non-null values are included.
 */
@Fluent
@JsonInclude(Include.NON_NULL)
public final class DigitalTwinMetadata {

    @JsonProperty(value = "$model", required = true)
    private String modelId;

    @JsonIgnore
    private final Map<String, Object> writableProperties = new HashMap<>();

    /**
     * Creates an instance of digital twin metadata.
     */
    public DigitalTwinMetadata() {
    }

    /**
     * Gets the Id of the model that the digital twin or component is modeled by.
     * @return The Id of the model that the digital twin or component is modeled by.
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Sets the Id of the model that the digital twin or component is modeled by.
     * @param modelId The Id of the model that the digital twin or component is modeled by.
     * @return The DigitalTwinMetadata object itself.
     */
    public DigitalTwinMetadata setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    /**
     * Gets the model-defined writable properties' request state.
     * For your convenience, the value of each map can be turned into an instance of {@link WritableProperty}.
     * @return The model-defined writable properties' request state.
     */
    @JsonAnyGetter
    public Map<String, Object> getWritableProperties() {
        return writableProperties;
    }

    /**
     * Adds additional writable properties to the model-defined writable properties' request state.
     * @param key The key of the additional property to be added to the component metadata.
     * @param value The value of the additional property to be added to the component metadata.
     * @return The DigitalTwinMetadata object itself.
     */
    @JsonAnySetter
    public DigitalTwinMetadata addWritableProperties(String key, Object value) {
        this.writableProperties.put(key, value);
        return this;
    }
}
