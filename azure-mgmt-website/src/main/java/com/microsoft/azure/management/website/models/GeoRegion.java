/**
 * Object]
 */

package com.microsoft.azure.management.website.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

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
    public String getGeoRegionName() {
        return this.geoRegionName;
    }

    /**
     * Set the geoRegionName value.
     *
     * @param geoRegionName the geoRegionName value to set
     */
    public void setGeoRegionName(String geoRegionName) {
        this.geoRegionName = geoRegionName;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
