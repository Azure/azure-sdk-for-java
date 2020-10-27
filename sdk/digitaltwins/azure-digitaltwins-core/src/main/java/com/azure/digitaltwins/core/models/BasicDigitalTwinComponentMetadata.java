// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * An optional, helper class for deserializing a digital twin component metadata object. This corresponds to the
 * {@link DigitalTwinsJsonPropertyNames#DIGITAL_TWIN_METADATA} property object on a {@link BasicDigitalTwinComponent}
 * <p>
 * Note that this class uses {@link JsonProperty} from the Jackson serialization library. Because of this, this type
 * will only work if the default json serializer is used by the digital twins client or if the custom json
 * serializer uses Jackson as well. In order to use a different json library, a new BasicDigitalTwinComponentMetadata class must
 * be constructed and have its json propertyMetadata tagged by the annotation used by that json library.
 */
@Fluent
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class BasicDigitalTwinComponentMetadata {
    @JsonIgnore
    private final Map<String, Object> propertyMetadata = new HashMap<>();

    /**
     * Gets the metadata about changes on properties on a component. The values can be deserialized into {@link BasicDigitalTwinPropertyMetadata}
     * @return
     */
    @JsonAnyGetter
    public Map<String, Object> getPropertyMetadata() {
        return propertyMetadata;
    }

    /**
     * Adds an additional custom property to the digital twin component. This field will contain any property
     * of the digital twin component that is not already defined by the other strong types of this class.
     * @param key The key of the additional property to be added to the digital twin.
     * @param value The value of the additional property to be added to the digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    @JsonAnySetter
    public BasicDigitalTwinComponentMetadata addPropertyMetadata(String key, Object value) {
        this.propertyMetadata.put(key, value);
        return this;
    }
}
