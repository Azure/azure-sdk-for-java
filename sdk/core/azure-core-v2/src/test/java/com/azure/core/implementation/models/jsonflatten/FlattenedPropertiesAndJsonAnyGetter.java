// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.models.jsonflatten;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Class with {@link JsonFlatten} on a property along with {@link JsonAnyGetter} on a method.
 */
@Fluent
public final class FlattenedPropertiesAndJsonAnyGetter {
    @JsonFlatten
    @JsonProperty("flattened.string")
    private String string;

    @JsonIgnore
    private Map<String, Object> additionalProperties;

    public FlattenedPropertiesAndJsonAnyGetter setString(String string) {
        this.string = string;
        return this;
    }

    public String getString() {
        return string;
    }

    @JsonAnyGetter
    public Map<String, Object> additionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public FlattenedPropertiesAndJsonAnyGetter addAdditionalProperty(String key, Object value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }

        additionalProperties.put(key, value);
        return this;
    }
}
