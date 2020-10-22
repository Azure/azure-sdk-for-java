// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private final Map<String, Object> writableProperties = new HashMap<>();

    /**
     * The public constructor for the ComponentMetadata.
     */
    public ComponentMetadata() {}

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
     * @return The ComponentMetadata object itself.
     */
    @JsonAnySetter
    public ComponentMetadata addWritableProperties(String key, Object value) {
        this.writableProperties.put(key, value);
        return this;
    }
}
