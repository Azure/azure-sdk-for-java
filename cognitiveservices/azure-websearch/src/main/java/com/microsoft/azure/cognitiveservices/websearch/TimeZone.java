/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines the data and time of one or more geographic locations.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = TimeZone.class)
@JsonTypeName("TimeZone")
public class TimeZone extends SearchResultsAnswer {
    /**
     * The data and time, in UTC, of the geographic location specified in the
     * query. If the query specified a specific geographic location (for
     * example, a city), this object contains the name of the geographic
     * location and the current date and time of the location, in UTC. If the
     * query specified a general geographic location, such as a state or
     * country, this object contains the date and time of the primary city or
     * state found in the specified state or country. If the location contains
     * additional time zones, the otherCityTimes field contains the data and
     * time of cities or states located in the other time zones.
     */
    @JsonProperty(value = "primaryCityTime", required = true)
    private TimeZoneTimeZoneInformation primaryCityTime;

    /**
     * A list of dates and times of nearby time zones.
     */
    @JsonProperty(value = "otherCityTimes", access = JsonProperty.Access.WRITE_ONLY)
    private List<TimeZoneTimeZoneInformation> otherCityTimes;

    /**
     * Get the primaryCityTime value.
     *
     * @return the primaryCityTime value
     */
    public TimeZoneTimeZoneInformation primaryCityTime() {
        return this.primaryCityTime;
    }

    /**
     * Set the primaryCityTime value.
     *
     * @param primaryCityTime the primaryCityTime value to set
     * @return the TimeZone object itself.
     */
    public TimeZone withPrimaryCityTime(TimeZoneTimeZoneInformation primaryCityTime) {
        this.primaryCityTime = primaryCityTime;
        return this;
    }

    /**
     * Get the otherCityTimes value.
     *
     * @return the otherCityTimes value
     */
    public List<TimeZoneTimeZoneInformation> otherCityTimes() {
        return this.otherCityTimes;
    }

}
