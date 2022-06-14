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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * An optional, helper class for deserializing a digital twin.
 * The $metadata class on a {@link BasicDigitalTwinComponent}.
 * Only properties with non-null values are included.
 */
@Fluent
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class BasicDigitalTwinComponentMetadata implements Map<String, DigitalTwinPropertyMetadata> {

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.METADATA_LAST_UPDATE_TIME)
    private OffsetDateTime lastUpdatedOn;

    @JsonIgnore
    private final Map<String, DigitalTwinPropertyMetadata> propertyMetadata = new HashMap<>();

    /**
     * Creates an instance of digital twin metadata.
     */
    public BasicDigitalTwinComponentMetadata() {
    }

	/**
     * Gets the date and time when the twin was last updated.
     * @return The date and time the twin was last updated.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }
    
    /**
     * Sets the date and time when the twin was last updated.
     * @param lastUpdatedOn The time the twin was last updated by the service.
     * @return The BasicDigitalTwinMetadata object itself.
     */
    public BasicDigitalTwinComponentMetadata setLastUpdatedOn(OffsetDateTime lastUpdatedOn) {
    	this.lastUpdatedOn = lastUpdatedOn;
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
    public BasicDigitalTwinComponentMetadata addPropertyMetadata(String key, DigitalTwinPropertyMetadata value) {
        this.propertyMetadata.put(key, value);
        return this;
    }

    @Override
	public int size() {
		return propertyMetadata.size();
	}

	@Override
	public boolean isEmpty() {
		return propertyMetadata.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return propertyMetadata.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return propertyMetadata.containsValue(value);
	}

	@Override
	public DigitalTwinPropertyMetadata get(Object key) {
		return propertyMetadata.get(key);
	}

	@Override
	public DigitalTwinPropertyMetadata put(String key, DigitalTwinPropertyMetadata value) {
		return propertyMetadata.put(key, value);
	}

	@Override
	public DigitalTwinPropertyMetadata remove(Object key) {
		return propertyMetadata.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends DigitalTwinPropertyMetadata> m) {
		propertyMetadata.putAll(m);
	}

	@Override
	public void clear() {
		propertyMetadata.clear();
	}

	@Override
	public Set<String> keySet() {
		return propertyMetadata.keySet();
	}

	@Override
	public Collection<DigitalTwinPropertyMetadata> values() {
		return propertyMetadata.values();
	}

	@Override
	public Set<Entry<String, DigitalTwinPropertyMetadata>> entrySet() {
		return propertyMetadata.entrySet();
	}
}
