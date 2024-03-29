// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.maps.weather.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Wind direction. */
@Fluent
public final class WindDirection {
    /*
     * Wind direction in Azimuth degrees,  starting at true North and continuing in clockwise direction. North is 0
     * degrees, east is 90 degrees, south is 180 degrees, west is 270 degrees. Possible values 0-359.
     */
    @JsonProperty(value = "degrees")
    private Integer degrees;

    /*
     * Direction abbreviation in the specified language.
     */
    @JsonProperty(value = "localizedDescription")
    private String description;

    /** Set default WindDirection constructor to private */
    private WindDirection() {}

    /**
     * Get the degrees property: Wind direction in Azimuth degrees, starting at true North and continuing in clockwise
     * direction. North is 0 degrees, east is 90 degrees, south is 180 degrees, west is 270 degrees. Possible values
     * 0-359.
     *
     * @return the degrees value.
     */
    public Integer getDegrees() {
        return this.degrees;
    }

    /**
     * Get the description property: Direction abbreviation in the specified language.
     *
     * @return the description value.
     */
    public String getDescription() {
        return this.description;
    }
}
