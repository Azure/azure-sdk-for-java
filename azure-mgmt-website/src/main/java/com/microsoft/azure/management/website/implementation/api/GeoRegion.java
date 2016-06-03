/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Geographical region.
 */
@JsonFlatten
public class GeoRegion extends Resource {
    /**
     * Region name.
     */
    @JsonProperty(value = "properties.name")
    private String geoRegionName;

    /**
     * Region description.
     */
    @JsonProperty(value = "properties.description")
    private String description;

    /**
     * Display name for region.
     */
    @JsonProperty(value = "properties.displayName")
    private String displayName;

    /**
     * Get the geoRegionName value.
     *
     * @return the geoRegionName value
     */
    public String geoRegionName() {
        return this.geoRegionName;
    }

    /**
     * Set the geoRegionName value.
     *
     * @param geoRegionName the geoRegionName value to set
     * @return the GeoRegion object itself.
     */
    public GeoRegion withGeoRegionName(String geoRegionName) {
        this.geoRegionName = geoRegionName;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the GeoRegion object itself.
     */
    public GeoRegion withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the GeoRegion object itself.
     */
    public GeoRegion withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

}
