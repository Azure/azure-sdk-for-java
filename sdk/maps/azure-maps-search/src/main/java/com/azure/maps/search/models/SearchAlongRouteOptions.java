// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.models.GeoLineString;

/**
 * Class holding optional parameters for Search along a route.
 */
public final class SearchAlongRouteOptions extends BaseSearchGeometryOptions<SearchAlongRouteOptions> {
    private List<String> brandFilter;
    private List<ElectricVehicleConnector> electricVehicleConnectorFilter;
    private int maxDetourTime;
    private GeoLineString route;

    /**
     * Create search along route options.
     * @param query the search query
     * @param maxDetourTime the maximum detour time allowed
     * @param route the {@code GeoLineString} geometry representing the route
     */
    public SearchAlongRouteOptions(String query, int maxDetourTime, GeoLineString route) {
        this.maxDetourTime = maxDetourTime;
        this.setQuery(query);
        this.setRoute(route);
    }

    /**
     * Returns the brand filter.
     * @return the brand filter.
     */
    public List<String> getBrandFilter() {
        return brandFilter;
    }

    /**
     * Returns the max detour time.
     * @return the max detour time.
     */
    public int getMaxDetourTime() {
        return this.maxDetourTime;
    }

    /**
     * Returns the route used in the search.
     * @return {@code GeoJsonLineString} return the route used in the search.
     */
    public GeoLineString getRoute() {
        return route;
    }

    /**
     * Returns the electric vehicle connector filter.
     * @return the electric vehicle connector filter.
     */
    public List<ElectricVehicleConnector> getElectricVehicleConnectorFilter() {
        return electricVehicleConnectorFilter;
    }

    /**
     * Sets the brand filter.
     * @param brandFilter the brand filter.
     * @return a reference to this {@code SearchAlongRouteOptions}
     */
    public SearchAlongRouteOptions setBrandFilter(List<String> brandFilter) {
        this.brandFilter = brandFilter;
        return this;
    }

    /**
     * Sets the route.
     * @param route the {@code GeoLineString} geometry representing the route.
     * @return a reference to this {@code SearchAlongRouteOptions}
     */
    public SearchAlongRouteOptions setRoute(GeoLineString route) {
        this.route = route;
        return this;
    }

    /**
     * Sets the electric vehicle connector filter.
     * @param electricVehicleConnectorFilter the electric vehicle connector filter.
     * @return a reference to this {@code SearchAlongRouteOptions}
     */
    public SearchAlongRouteOptions setElectricVehicleConnectorFilter(List<ElectricVehicleConnector> electricVehicleConnectorFilter) {
        this.electricVehicleConnectorFilter = electricVehicleConnectorFilter;
        return this;
    }

    /**
     * Sets the max detour time.
     * @param maxDetourTime the max detour time.
     * @return a reference to this {@code SearchAlongRouteOptions}
     */
    public SearchAlongRouteOptions setMaxDetourTime(int maxDetourTime) {
        this.maxDetourTime = maxDetourTime;
        return this;
    }
}
