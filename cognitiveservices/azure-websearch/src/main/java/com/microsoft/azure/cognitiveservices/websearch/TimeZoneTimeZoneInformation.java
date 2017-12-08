/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a date and time for a geographical location.
 */
public class TimeZoneTimeZoneInformation {
    /**
     * The name of the geographical location.For example, County; City; City,
     * State; City, State, Country; or Time Zone.
     */
    @JsonProperty(value = "location", required = true)
    private String location;

    /**
     * The data and time specified in the form, YYYY-MM-DDThh;mm:ss.ssssssZ.
     */
    @JsonProperty(value = "time", required = true)
    private String time;

    /**
     * The offset from UTC. For example, UTC-7.
     */
    @JsonProperty(value = "utcOffset", required = true)
    private String utcOffset;

    /**
     * Get the location value.
     *
     * @return the location value
     */
    public String location() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the location value to set
     * @return the TimeZoneTimeZoneInformation object itself.
     */
    public TimeZoneTimeZoneInformation withLocation(String location) {
        this.location = location;
        return this;
    }

    /**
     * Get the time value.
     *
     * @return the time value
     */
    public String time() {
        return this.time;
    }

    /**
     * Set the time value.
     *
     * @param time the time value to set
     * @return the TimeZoneTimeZoneInformation object itself.
     */
    public TimeZoneTimeZoneInformation withTime(String time) {
        this.time = time;
        return this;
    }

    /**
     * Get the utcOffset value.
     *
     * @return the utcOffset value
     */
    public String utcOffset() {
        return this.utcOffset;
    }

    /**
     * Set the utcOffset value.
     *
     * @param utcOffset the utcOffset value to set
     * @return the TimeZoneTimeZoneInformation object itself.
     */
    public TimeZoneTimeZoneInformation withUtcOffset(String utcOffset) {
        this.utcOffset = utcOffset;
        return this;
    }

}
