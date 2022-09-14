package com.azure.maps.timezone.models;

import java.time.OffsetDateTime;
/**
 * Organizes inputs for get timezone by ID
 */
public final class TimezoneIdOptions {
    private String timezoneId;
    private String acceptLanguage;
    private TimezoneOptions options;
    private OffsetDateTime timeStamp;
    private OffsetDateTime daylightSavingsTimeFrom;
    private Integer daylightSavingsTimeLastingYears;

    /**
     * get timezoneId
     * @return
     */
    public String getTimezoneId() {
        return timezoneId;
    }

    /**
     * Set timezone id
     * @param timezoneId
     * @return
     */
    public TimezoneIdOptions setTimezoneId(String timezoneId) {
        this.timezoneId = timezoneId;
        return this;
    }

    /**
     * get accept language
     * @return
     */
    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    /**
     * set accept language
     * @param acceptLanguage
     * @return
     */
    public TimezoneIdOptions setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /**
     * get timezone options
     * @return
     */
    public TimezoneOptions getOptions() {
        return options;
    }

    /**
     * set timezone options
     * @param options
     * @return
     */
    public TimezoneIdOptions setOptions(TimezoneOptions options) {
        this.options = options;
        return this;
    }

    /**
     * get time stamp
     * @return
     */
    public OffsetDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * set time stamp
     * @param timeStamp
     * @return
     */
    public TimezoneIdOptions setTimeStamp(OffsetDateTime timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * get daylight savings time from
     * @return
     */
    public OffsetDateTime getDaylightSavingsTimeFrom() {
        return daylightSavingsTimeFrom;
    }

    /**
     * set daylight savings time from
     * @param daylightSavingsTimeFrom
     * @return
     */
    public TimezoneIdOptions setDaylightSavingsTimeFrom(OffsetDateTime daylightSavingsTimeFrom) {
        this.daylightSavingsTimeFrom = daylightSavingsTimeFrom;
        return this;
    }

    /**
     * get daylight savings time lasting years
     * @return
     */
    public Integer getDaylightSavingsTimeLastingYears() {
        return daylightSavingsTimeLastingYears;
    }

    /**
     * set daylight savings time lasting years
     * @param daylightSavingsTimeLastingYears
     * @return
     */
    public TimezoneIdOptions setDaylightSavingsTimeLastingYears(Integer daylightSavingsTimeLastingYears) {
        this.daylightSavingsTimeLastingYears = daylightSavingsTimeLastingYears;
        return this;
    }
}
