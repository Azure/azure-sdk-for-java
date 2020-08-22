// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.serialization;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * An optional, helper class for deserializing a digital twin.
 * The $metadata class on a {@link BasicDigitalTwin}.
 */
@Fluent
public class DigitalTwinMetadata {

    @JsonProperty(value = "$model", required = true)
    private String modelId;

    private Map<String, Object> writeableProperties;

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
     * @return The model-defined writable properties' request state.
     */
    @JsonAnyGetter
    public Map<String, Object> getWriteableProperties() {
        return writeableProperties;
    }
}
