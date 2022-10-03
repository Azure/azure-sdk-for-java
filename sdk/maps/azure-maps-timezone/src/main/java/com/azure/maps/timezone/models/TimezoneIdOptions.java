// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// See License.txt in the project root for license information.

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
     * TimezoneIdOptions constructor
     * @param timezoneId the IANA time zone ID.
     */
    public TimezoneIdOptions(String timezoneId) {
        this.timezoneId = timezoneId;
    }

    /**
     * get timezoneId
     * @return the timezone id, the IANA time zone ID.
     */
    public String getTimezoneId() {
        return timezoneId;
    }

    /**
     * Set timezone id
     * @param timezoneId the IANA time zone ID.
     * @return TimezoneIdOptions
     */
    public TimezoneIdOptions setTimezoneId(String timezoneId) {
        this.timezoneId = timezoneId;
        return this;
    }

    /**
     * get accept language
     * @return the accept language. Specifies the language code in which the timezone names should be returned. If no language
     *     code is provided, the response will be in "EN". Please refer to [Supported
     *     Languages](https://docs.microsoft.com/azure/azure-maps/supported-languages) for details.
     */
    public String getLanguage() {
        return acceptLanguage;
    }

    /**
     * set accept language
     * @param acceptLanguage Specifies the language code in which the timezone names should be returned. If no language
     *     code is provided, the response will be in "EN". Please refer to [Supported
     *     Languages](https://docs.microsoft.com/azure/azure-maps/supported-languages) for details.
     * @return TimezoneIdOptions
     */
    public TimezoneIdOptions setLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
        return this;
    }

    /**
     * get timezone options
     * @return TimezoneOptions. Alternatively, use alias "o". Options available for types of information returned in the result.
     */
    public TimezoneOptions getOptions() {
        return options;
    }

    /**
     * set timezone options
     * @param options Alternatively, use alias "o". Options available for types of information returned in the result.
     * @return TimezoneIdOptions
     */
    public TimezoneIdOptions setOptions(TimezoneOptions options) {
        this.options = options;
        return this;
    }

    /**
     * get time stamp
     * @return the time stamp. Alternatively, use alias "stamp", or "s". Reference time, if omitted, the API will use the
     *     machine time serving the request.
     */
    public OffsetDateTime getTimestamp() {
        return timeStamp;
    }

    /**
     * set time stamp
     * @param timeStamp Alternatively, use alias "stamp", or "s". Reference time, if omitted, the API will use the
     *     machine time serving the request.
     * @return TimezoneIdOptions
     */
    public TimezoneIdOptions setTimestamp(OffsetDateTime timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * get daylight savings time from
     * @return the daylight savings time in offset date time. Alternatively, use alias "tf". The start date from which daylight savings time
     *     (DST) transitions are requested, only applies when "options" = all or "options" = transitions.
     */
    public OffsetDateTime getDaylightSavingsTime() {
        return daylightSavingsTimeFrom;
    }

    /**
     * set daylight savings time from
     * @param daylightSavingsTimeFrom Alternatively, use alias "tf". The start date from which daylight savings time
     *     (DST) transitions are requested, only applies when "options" = all or "options" = transitions.
     * @return TimezoneIdOptions
     */
    public TimezoneIdOptions setDaylightSavingsTime(OffsetDateTime daylightSavingsTimeFrom) {
        this.daylightSavingsTimeFrom = daylightSavingsTimeFrom;
        return this;
    }

    /**
     * get daylight savings time lasting years
     * @return the daylight savings time in lasting years. Alternatively, use alias "ty". The number of years from "transitionsFrom"
     *     for which DST transitions are requested, only applies when "options" = all or "options" = transitions.
     */
    public Integer getDaylightSavingsTimeLastingYears() {
        return daylightSavingsTimeLastingYears;
    }

    /**
     * set daylight savings time lasting years
     * @param daylightSavingsTimeLastingYears Alternatively, use alias "ty". The number of years from "transitionsFrom"
     *     for which DST transitions are requested, only applies when "options" = all or "options" = transitions.
     * @return TimezoneIdOptions
     */
    public TimezoneIdOptions setDaylightSavingsTimeLastingYears(Integer daylightSavingsTimeLastingYears) {
        this.daylightSavingsTimeLastingYears = daylightSavingsTimeLastingYears;
        return this;
    }
}
