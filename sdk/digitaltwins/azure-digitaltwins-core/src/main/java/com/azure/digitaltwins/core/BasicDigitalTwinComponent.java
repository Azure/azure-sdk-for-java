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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.OffsetDateTime;
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
    @JsonProperty(value = DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA, required = true)
    private Map<String, DigitalTwinPropertyMetadata> metadata = new HashMap<>();

    /**
     * The time and date the component was last updated.
     */
    @JsonIgnore
    private OffsetDateTime lastUpdatedOn;
   
    /**
     * The additional contents of the model. This field will contain any contents of the digital twin that are not already defined by the other strong types of this class.
     */
    @JsonIgnore
    private final Map<String, Object> contents = new HashMap<>();
    
    /**
     * Construct an empty digital twin component.
     */
    public BasicDigitalTwinComponent() {
    }
    
    /**
     * Construct a digital twin component, specifying the time the component was last updated. This is primarily
     * added to aid in deserialization.
     * @param lastUpdatedOn The date and time the digital twin was last updated.
     */
    public BasicDigitalTwinComponent(OffsetDateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    /**
     * Gets the metadata about the model.
     * @return The component metadata.
     */
    public Map<String, DigitalTwinPropertyMetadata> getMetadata() {
        return metadata;
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
     * Gets the date and time when the twin was last updated.
     * @return The date and time the twin was last updated.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
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

    /**
     * Unwraps the raw metadata received from the service and extracts the "$lastUpdateTime" property.
     * @param metadata The metadata of the component.
     */
    @JsonProperty(value = DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA)
    private void unwrapMetadata(Map<String, Object> metadata) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        String lastUpdatedOnString;
        if ((lastUpdatedOnString = (String) metadata.get(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME)) != null) {
            this.lastUpdatedOn = OffsetDateTime.parse(lastUpdatedOnString);
            metadata.remove(DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME);
        }
        
        this.metadata = mapper.convertValue(metadata, (Class<? extends Map<String, DigitalTwinPropertyMetadata>>) Map.class);
    }
}
