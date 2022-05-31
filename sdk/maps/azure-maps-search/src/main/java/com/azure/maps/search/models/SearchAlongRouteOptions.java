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
     */
    public SearchAlongRouteOptions(String query, int maxDetourTime, GeoLineString route) {
        this.maxDetourTime = maxDetourTime;
        this.setQuery(query);
        this.setRoute(route);
    }

    /**
     * Returns the brand filter.
     * @return
     */
    public List<String> getBrandFilter() {
        return brandFilter;
    }

    /**
     * Returns the max detour time.
     */
    public int getMaxDetourTime() {
        return this.maxDetourTime;
    }

    /**
     * @return GeoJsonLineString return the route
     */
    public GeoLineString getRoute() {
        return route;
    }

    /**
     * Returns the electric vehicle connector filter.
     * @return
     */
    public List<ElectricVehicleConnector> getElectricVehicleConnectorFilter() {
        return electricVehicleConnectorFilter;
    }

    /**
     * Sets the brand filter.
     * @param brandFilter
     * @return
     */
    public SearchAlongRouteOptions setBrandFilter(List<String> brandFilter) {
        this.brandFilter = brandFilter;
        return this;
    }

    /**
     * Sets the route.
     * @param route
     * @return
     */
    public SearchAlongRouteOptions setRoute(GeoLineString route) {
        this.route = route;
        return this;
    }

    /**
     * Sets the electric vehicle connector filter.
     * @param electricVehicleConnectorFilter
     * @return
     */
    public SearchAlongRouteOptions setElectricVehicleConnectorFilter(List<ElectricVehicleConnector> electricVehicleConnectorFilter) {
        this.electricVehicleConnectorFilter = electricVehicleConnectorFilter;
        return this;
    }

    /**
     * Sets the max detour time.
     * @param maxDetourTime
     * @return
     */
    public SearchAlongRouteOptions setMaxDetourTime(int maxDetourTime) {
        this.maxDetourTime = maxDetourTime;
        return this;
    }
}
