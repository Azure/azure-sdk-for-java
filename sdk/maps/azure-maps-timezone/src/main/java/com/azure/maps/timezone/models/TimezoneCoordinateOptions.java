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
     * Get coordinates
     * @return GeoPosition coordinate
     */
    public GeoPosition getCoordinates() {
        return coordinates;
    }

    /**
     * Set geoposition coordinate
     * @param coordinates
     * @return
     */
    public TimezoneCoordinateOptions setCoordinates(GeoPosition coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    /**
     * Get accept language
     * @return
     */
    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    /**
     * Set accept language
     * @param acceptLanguage
     * @return
     */
    public TimezoneCoordinateOptions setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /**
     * Get timezone options
     * @return
     */
    public TimezoneOptions getOptions() {
        return options;
    }

    /**
     * Set timezone options
     * @param options
     * @return
     */
    public TimezoneCoordinateOptions setOptions(TimezoneOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Get timestamp
     * @return
     */
    public OffsetDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * Set timestamp
     * @param timeStamp
     * @return
     */
    public TimezoneCoordinateOptions setTimeStamp(OffsetDateTime timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * Get daylight savings time from
     * @return
     */
    public OffsetDateTime getDaylightSavingsTimeFrom() {
        return daylightSavingsTimeFrom;
    }

    /**
     * Set daylight savings time from
     * @param daylightSavingsTimeFrom
     * @return
     */
    public TimezoneCoordinateOptions setDaylightSavingsTimeFrom(OffsetDateTime daylightSavingsTimeFrom) {
        this.daylightSavingsTimeFrom = daylightSavingsTimeFrom;
        return this;
    }

    /**
     * Get daylight savings time lasting years
     * @return
     */
    public Integer getDaylightSavingsTimeLastingYears() {
        return daylightSavingsTimeLastingYears;
    }

    /**
     * Set daylight savings time lasting years
     * @param daylightSavingsTimeLastingYears
     * @return
     */
    public TimezoneCoordinateOptions setDaylightSavingsTimeLastingYears(Integer daylightSavingsTimeLastingYears) {
        this.daylightSavingsTimeLastingYears = daylightSavingsTimeLastingYears;
        return this;
    }
}
