// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See License.txt in the project root for license information.

package com.azure.maps.timezone.models;

import java.time.OffsetDateTime;

import com.azure.core.models.GeoPosition;
/**
 * Organizes inputs for get timezone by coordinate
 */
public final class TimezoneCoordinateOptions {
    private GeoPosition coordinates;
    private String acceptLanguage;
    private TimezoneOptions options;
    private OffsetDateTime timeStamp;
    private OffsetDateTime daylightSavingsTimeFrom;
    private Integer daylightSavingsTimeLastingYears;

    /**
     * TimezoneCoordinateOptions constructor
     * @param coordinates GeoPosition coordinates of the point for which time zone information is requested. This parameter is a
     *     list of coordinates, containing a pair of coordinate(lon, lat). When this endpoint is called directly,
     *     coordinates are passed in as a single string containing coordinates, separated by commas.
     */
    public TimezoneCoordinateOptions(GeoPosition coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Get coordinates
     * @return GeoPosition coordinates of the point for which time zone information is requested. This parameter is a
     *     list of coordinates, containing a pair of coordinate(lon, lat). When this endpoint is called directly,
     *     coordinates are passed in as a single string containing coordinates, separated by commas.
     */
    public GeoPosition getPosition() {
        return coordinates;
    }

    /**
     * Set geoposition coordinate
     * @param coordinates GeoPosition coordinates of the point for which time zone information is requested. This parameter is a
     *     list of coordinates, containing a pair of coordinate(lon, lat). When this endpoint is called directly,
     *     coordinates are passed in as a single string containing coordinates, separated by commas.
     * @return returns TimezoneCoordinateOptions
     */
    public TimezoneCoordinateOptions setPosition(GeoPosition coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    /**
     * Get accept language
     * @return Specifies the language code in which the timezone names should be returned. If no language
     *     code is provided, the response will be in "EN". Please refer to [Supported
     *     Languages](https://docs.microsoft.com/azure/azure-maps/supported-languages) for details.
     */
    public String getLanguage() {
        return acceptLanguage;
    }

    /**
     * Set accept language
     * @param acceptLanguage Specifies the language code in which the timezone names should be returned. If no language
     *     code is provided, the response will be in "EN". Please refer to [Supported
     *     Languages](https://docs.microsoft.com/azure/azure-maps/supported-languages) for details.
     * @return TimezoneCoordinateOptions
     */
    public TimezoneCoordinateOptions setLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /**
     * Get timezone options
     * @return returns timezone options. Alternatively, use alias "o". Options available for types of information returned in the result.
     */
    public TimezoneOptions getTimezoneOptions() {
        return options;
    }

    /**
     * Set timezone options
     * @param options Alternatively, use alias "o". Options available for types of information returned in the result.
     * @return TimezoneCoordinateOptions
     */
    public TimezoneCoordinateOptions setTimezoneOptions(TimezoneOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Get timestamp
     * @return the time stamp. Alternatively, use alias "stamp", or "s". Reference time, if omitted, the API will use the
     *     machine time serving the request.
     */
    public OffsetDateTime getTimestamp() {
        return timeStamp;
    }

    /**
     * Set timestamp
     * @param timeStamp Alternatively, use alias "stamp", or "s". Reference time, if omitted, the API will use the
     *     machine time serving the request.
     * @return TimezoneCoordinateOptions
     */
    public TimezoneCoordinateOptions setTimestamp(OffsetDateTime timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * Get daylight savings time from
     * @return daylight savings time. Alternatively, use alias "tf". The start date from which daylight savings time
     *     (DST) transitions are requested, only applies when "options" = all or "options" = transitions.
     */
    public OffsetDateTime getDaylightSavingsTime() {
        return daylightSavingsTimeFrom;
    }

    /**
     * Set daylight savings time from
     * @param daylightSavingsTimeFrom Alternatively, use alias "tf". The start date from which daylight savings time
     *     (DST) transitions are requested, only applies when "options" = all or "options" = transitions.
     * @return TimezoneCoordinateOptions
     */
    public TimezoneCoordinateOptions setDaylightSavingsTime(OffsetDateTime daylightSavingsTimeFrom) {
        this.daylightSavingsTimeFrom = daylightSavingsTimeFrom;
        return this;
    }

    /**
     * Get daylight savings time lasting years
     * @return daylight savings time in lasting years. Alternatively, use alias "ty". The number of years from "transitionsFrom"
     *     for which DST transitions are requested, only applies when "options" = all or "options" = transitions.
     */
    public Integer getDaylightSavingsTimeLastingYears() {
        return daylightSavingsTimeLastingYears;
    }

    /**
     * Set daylight savings time lasting years
     * @param daylightSavingsTimeLastingYears Alternatively, use alias "ty". The number of years from "transitionsFrom"
     *     for which DST transitions are requested, only applies when "options" = all or "options" = transitions.
     * @return TimezoneCoordinateOptions
     */
    public TimezoneCoordinateOptions setDaylightSavingsTimeLastingYears(Integer daylightSavingsTimeLastingYears) {
        this.daylightSavingsTimeLastingYears = daylightSavingsTimeLastingYears;
        return this;
    }
}
