// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.HashMap;
import java.util.Map;

/**
 * Properties on a component that adhere to a specific model.
 */
@Fluent
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class BasicDigitalTwinComponent {

    /**
     * Information about the model a component conforms to. This field is present on every digital twin.
     */
    @JsonProperty(value = "$metadata", required = true)
    @JsonDeserialize(as = BasicDigitalTwinComponentMetadata.class)
    private Map<String, DigitalTwinPropertyMetadata> metadata = new BasicDigitalTwinComponentMetadata();

    /**
     * The additional contents of the model. This field will contain any contents of the digital twin that are not already defined by the other strong types of this class.
     */
    @JsonIgnore
    private final Map<String, Object> contents = new HashMap<>();

    /**
     * Gets the metadata about the model.
     * @return The component metadata.
     */
    public Map<String, DigitalTwinPropertyMetadata> getMetadata() {
        return metadata;
    }
    
    /**
     * Sets the model metadata.
     * @param metadata Component metadata.
     * @return The BasicDigitalTwinComponent object itself.
     */
    public BasicDigitalTwinComponent setMetadata(BasicDigitalTwinComponentMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Adds property metadata.
     * @param key The key that maps to the property metadata
     * @param metadata Property metadata.
     * @return The BasicDigitalTwinComponent object itself.
     */
    public BasicDigitalTwinComponent addMetadata(String key, DigitalTwinPropertyMetadata metadata) {
        this.metadata.put(key, metadata);
        return this;
    }

    /**
     * Gets the custom contents
     * @return The custom contents
     */
    @JsonAnyGetter
    public Map<String, Object> getContents() {
        return contents;
    }

    /**
     * Adds additional custom property to the component's contents.
     * @param key The key of the additional property to be added to the component's contents.
     * @param value The value of the additional property to be added to the component's contents.
     * @return The BasicDigitalTwinComponent object itself.
     */
    @JsonAnySetter
    public BasicDigitalTwinComponent addToContents(String key, Object value) {
        this.contents.put(key, value);
        return this;
    }
}
