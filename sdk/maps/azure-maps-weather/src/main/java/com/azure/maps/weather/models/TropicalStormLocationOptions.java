// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.models;

/**
 * TropicalStormLocationOptions class
 */
public final class TropicalStormLocationOptions {
    private Integer year;
    private BasinId basinId;
    private Integer governmentStormId;
    private WeatherDataUnit unit;
    private Boolean includeDetails;
    private Boolean includeGeometricDetails;
    private Boolean includeCurrentStorm;

    /**
     * TropicalStormLocationOptions constructor
     *
     * @param year Year of the cyclone(s).
     * @param basinId Basin identifier.
     * @param governmentStormId Government storm Id.
     */
    public TropicalStormLocationOptions(Integer year, BasinId basinId, Integer governmentStormId) {
        this.year = year;
        this.basinId = basinId;
        this.governmentStormId = governmentStormId;
    }

    /**
     * Get year of the cyclone(s).
     *
     * @return Year of the cyclone(s).
     */
    public Integer getYear() {
        return this.year;
    }

    /**
     * Get basin identifier.
     *
     * @return Basin identifier.
     */
    public BasinId getBasinId() {
        return this.basinId;
    }

    /**
     * Get government storm Id.
     *
     * @return Government storm Id.
     */
    public Integer getGovernmentStormId() {
        return this.governmentStormId;
    }

    /**
     * Get WeatherDataUnit
     *
     * @return WeatherDataUnit - specifies to return the data in either metric units or imperial units. Default value is metric.
     */
    public WeatherDataUnit getUnit() {
        return this.unit;
    }

    /**
     * Get includeDetails
     *
     * @return includeDetails - when true, wind radii summary data is included in the response.
     */
    public Boolean getIncludeDetails() {
        return this.includeDetails;
    }

    /**
     * Get includeGeometricDetails
     *
     * @return includeGeometricDetails - when true, wind radii summary data and geoJSON details are included in the
     * response.
     */
    public Boolean getIncludeGeometricDetails() {
        return this.includeGeometricDetails;
    }

    /**
     * Get includeCurrentStorm
     *
     * @return includeCurrentStorm - when true, return the current storm location.
     */
    public Boolean getIncludeCurrentStorm() {
        return this.includeCurrentStorm;
    }

    /**
     * Set year of the cyclone(s)
     *
     * @param year Year of the cyclone(s).
     * @return TropicalStormLocationOptions
     */
    public TropicalStormLocationOptions setYear(Integer year) {
        this.year = year;
        return this;
    }

    /**
     * Set basin identifier.
     *
     * @param basinId Basin identifier.
     * @return TropicalStormLocationOptions
     */
    public TropicalStormLocationOptions setBasinId(BasinId basinId) {
        this.basinId = basinId;
        return this;
    }

    /**
     * Set government storm Id.
     *
     * @param governmentStormId Government storm Id.
     * @return TropicalStormLocationOptions
     */
    public TropicalStormLocationOptions setGovernmentStormId(Integer governmentStormId) {
        this.governmentStormId = governmentStormId;
        return this;
    }

    /**
     * Set WeatherDataUnit
     *
     * @param unit Specifies to return the data in either metric units or imperial units. Default value is metric.
     * @return TropicalStormLocationOptions
     */
    public TropicalStormLocationOptions setUnit(WeatherDataUnit unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Set includeDetails
     *
     * @param includeDetails When true, wind radii summary data is included in the response.
     * @return TropicalStormLocationOptions
     */
    public TropicalStormLocationOptions setIncludeDetails(Boolean includeDetails) {
        this.includeDetails = includeDetails;
        return this;
    }

    /**
     * Set includeGeometricDetails
     *
     * @param includeGeometricDetails When true, wind radii summary data and geoJSON details are included in the
     * response.
     * @return TropicalStormLocationOptions
     */
    public TropicalStormLocationOptions setIncludeGeometricDetails(Boolean includeGeometricDetails) {
        this.includeGeometricDetails = includeGeometricDetails;
        return this;
    }

    /**
     * Set includeCurrentStorm
     * 
     * @param includeCurrentStorm When true, return the current storm location.
     * @return TropicalStormLocationOptions
     */
    public TropicalStormLocationOptions setIncludeCurrentStorm(Boolean includeCurrentStorm) {
        this.includeCurrentStorm = includeCurrentStorm;
        return this;
    }
}
