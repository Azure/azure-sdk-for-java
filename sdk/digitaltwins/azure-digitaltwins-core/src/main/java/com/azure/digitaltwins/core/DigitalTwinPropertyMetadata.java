// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Contains metadata about changes on properties on a digital twin or component.
 */
@Fluent
public final class DigitalTwinPropertyMetadata {
    @JsonProperty(value = DigitalTwinsJsonPropertyNames.METADATA_PROPERTY_LAST_UPDATE_TIME, required = true)
    private OffsetDateTime lastUpdatedOn;

    /**
     * Gets the date and time the property was last updated.
     * @return The date and time the property was last updated.
     */
    public OffsetDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    /**
     * Sets the the date and time the property was last updated.
     * @param lastUpdatedOn The date and time the property was last updated.
     * @return The DigitalTwinPropertyMetadata object itself.
     */
    public DigitalTwinPropertyMetadata setLastUpdatedOn(OffsetDateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
        return this;
    }
}
