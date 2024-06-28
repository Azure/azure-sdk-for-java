// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.models;

/**
 * TropicalStormForecastOptions class
 */
public final class TropicalStormForecastOptions {
    private Integer year;
    private BasinId basinId;
    private Integer governmentStormId;
    private WeatherDataUnit unit;
    private Boolean includeDetails;
    private Boolean includeGeometricDetails;
    private Boolean includeWindowGeometry;

    /**
     * TropicalStormForecastOptions constructor
     *
     * @param year Year of the cyclone(s).
     * @param basinId Basin identifier.
     * @param governmentStormId Government storm Id.
     */
    public TropicalStormForecastOptions(Integer year, BasinId basinId, Integer governmentStormId) {
        this.year = year;
        this.basinId = basinId;
        this.governmentStormId = governmentStormId;
    }

    /**
     * Get year of the cyclone(s) as integer
     *
     * @return Year of the cyclone(s).
     */
    public Integer getYear() {
        return this.year;
    }

    /**
     * Get basin identifier
     *
     * @return basin identifier
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
     * @return getIncludeGeometricDetails - when true, wind radii summary data and geoJSON details are included in the
     * response.
     */
    public Boolean getIncludeGeometricDetails() {
        return this.includeGeometricDetails;
    }

    /**
     * Get includeWindowsGeometry
     *
     * @return includeWindowGeometry - When true, window geometry data (geoJSON) is included in the response.
     */
    public Boolean getIncludeWindowGeometry() {
        return this.includeWindowGeometry;
    }

    /**
     * Set year of the cyclone(s)
     *
     * @param year Year of the cyclone(s).
     * @return TropicalStormForecastOptions
     */
    public TropicalStormForecastOptions setYear(Integer year) {
        this.year = year;
        return this;
    }

    /**
     * Set basin identifier
     *
     * @param basinId Basin identifier.
     * @return TropicalStormForecastOptions
     */
    public TropicalStormForecastOptions setBasinId(BasinId basinId) {
        this.basinId = basinId;
        return this;
    }

    /**
     * Set government storm Id.
     *
     * @param governmentStormId Government storm Id.
     * @return TropicalStormForecastOptions
     */
    public TropicalStormForecastOptions setGovernmentStormId(Integer governmentStormId) {
        this.governmentStormId = governmentStormId;
        return this;
    }

    /**
     * Set WeatherDataUnit
     *
     * @param unit Specifies to return the data in either metric units or imperial units. Default value is metric.
     * @return TropicalStormForecastOptions
     */
    public TropicalStormForecastOptions setUnit(WeatherDataUnit unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Set includeDetails
     *
     * @param includeDetails When true, wind radii summary data is included in the response.
     * @return TropicalStormForecastOptions
     */
    public TropicalStormForecastOptions setIncludeDetails(Boolean includeDetails) {
        this.includeDetails = includeDetails;
        return this;
    }

    /**
     * Set includeGeometricDetails
     *
     * @param includeGeometricDetails When true, wind radii summary data and geoJSON details are included in the
     * response.
     * @return TropicalStormForecastOptions
     */
    public TropicalStormForecastOptions setIncludeGeometricDetails(Boolean includeGeometricDetails) {
        this.includeGeometricDetails = includeGeometricDetails;
        return this;
    }

    /**
     * Set includeWindowsGeometry
     *
     * @param includeWindowGeometry When true, window geometry data (geoJSON) is included in the response.
     * @return TropicalStormForecastOptions
     */
    public TropicalStormForecastOptions setIncludeWindowGeometry(Boolean includeWindowGeometry) {
        this.includeWindowGeometry = includeWindowGeometry;
        return this;
    }
}
