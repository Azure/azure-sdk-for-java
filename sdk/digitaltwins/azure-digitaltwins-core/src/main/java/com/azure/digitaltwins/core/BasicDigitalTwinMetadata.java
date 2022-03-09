// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * An optional, helper class for deserializing a digital twin.
 * The $metadata class on a {@link BasicDigitalTwin}.
 * Only properties with non-null values are included.
 */
@Fluent
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class BasicDigitalTwinMetadata {

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.METADATA_MODEL, required = true)
    private String modelId;

    @JsonIgnore
    private final Map<String, DigitalTwinPropertyMetadata> propertyMetadata = new HashMap<>();

    /**
     * Creates an instance of digital twin metadata.
     */
    public BasicDigitalTwinMetadata() {
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
     * @return The BasicDigitalTwinMetadata object itself.
     */
    public BasicDigitalTwinMetadata setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    /**
     * Gets the metadata about changes on properties on a component. The values can be deserialized into {@link DigitalTwinPropertyMetadata}
     * @return The metadata about changes on properties on a component.
     */
    @JsonAnyGetter
    public Map<String, DigitalTwinPropertyMetadata> getPropertyMetadata() {
        return propertyMetadata;
    }

    /**
     * Adds an additional custom property to the digital twin. This field will contain any property
     * of the digital twin that is not already defined by the other strong types of this class.
     * @param key The key of the additional property to be added to the digital twin.
     * @param value The value of the additional property to be added to the digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    @JsonAnySetter
    public BasicDigitalTwinMetadata addPropertyMetadata(String key, DigitalTwinPropertyMetadata value) {
        this.propertyMetadata.put(key, value);
        return this;
    }
}
