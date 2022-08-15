// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.models;

import java.time.OffsetDateTime;
import java.util.List;

import com.azure.core.models.GeoPosition;

/**
 * Route Directions Options
 *
 */
public class RouteDirectionsOptions extends BaseRouteOptions<RouteDirectionsOptions> {
    private List<GeoPosition> routePoints;
    private Integer maxAlternatives;
    private AlternativeRouteType alternativeType;
    private Integer minDeviationDistance;
    private OffsetDateTime arriveAt;
    private Integer minDeviationTime;
    private RouteInstructionsType instructionsType;
    private String language;
    private Boolean computeBestWaypointOrder;
    private RouteRepresentationForBestOrder routeRepresentationForBestOrder;
    private ComputeTravelTime computeTravelTime;
    private Integer vehicleHeading;
    private Report report;
    private SectionType filterSectionType;

    /**
     * Builds a new set of options for the Route Directions call.
     *
     * @param routePoints a list of {@code GeoPosition} route points.
     */
    public RouteDirectionsOptions(List<GeoPosition> routePoints) {
        this.routePoints = routePoints;
    }

    /**
     * Returns the route points
     *
     * @return a list of {@code GeoPosition} route points.
     */
    public List<GeoPosition> getRoutePoints() {
        return this.routePoints;
    }

    /**
     * Returns the maximum number of alternative routes.
     *
     * @return the maximum number of alternative routes.
     */
    public Integer getMaxAlternatives() {
        return this.maxAlternatives;
    }

    /**
     * Returns the type of the alternative route
     *
     * @return a {@code AlternativeRouteType}
     */
    public AlternativeRouteType getAlternativeType() {
        return this.alternativeType;
    }

    /**
     * Returns the minimum deviation distance.
     *
     * @return the minimum deviation distance.
     */
    public Integer getMinDeviationDistance() {
        return this.minDeviationDistance;
    }

    /**
     * Returns the arrival time.
     *
     * @return the arrival time.
     */
    public OffsetDateTime getArriveAt() {
        return this.arriveAt;
    }

    /**
     * Returns the minimal deviation time.
     *
     * @return minimum deviation time.
     */
    public Integer getMinDeviationTime() {
        return this.minDeviationTime;
    }

    /**
     * Returns the type of route instructions.
     *
     * @return {@code RouteInstructionsType}
     */
    public RouteInstructionsType getInstructionsType() {
        return this.instructionsType;
    }

    /**
     * Returns the language.
     *
     * @return the language.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Returns whether to use the best waypoint order.
     *
     * @return whether to use the best waypoint order.
     */
    public Boolean getComputeBestWaypointOrder() {
        return this.computeBestWaypointOrder;
    }

    /**
     * Returns the representation for the best order.
     *
     * @return {@code RouteRepresentationForBestOrder}
     */
    public RouteRepresentationForBestOrder getRouteRepresentationForBestOrder() {
        return this.routeRepresentationForBestOrder;
    }

    /**
     * Returns the type of computation for the travel time.
     *
     * @return {@code ComputeTravelTime}
     */
    public ComputeTravelTime getComputeTravelTime() {
        return this.computeTravelTime;
    }

    /**
     * Returns the vehicle heading.
     *
     * @return the vehicle heading.
     */
    public Integer getVehicleHeading() {
        return this.vehicleHeading;
    }

    /**
     * Returns the type of {@code Report}
     *
     * @return {@code Report}
     */
    public Report getReport() {
        return this.report;
    }

    /**
     * Returns the type of {@code SectionType} used for filtering.
     *
     * @return {@code SectionType}
     */
    public SectionType getFilterSectionType() {
        return this.filterSectionType;
    }

    /**
     * Sets the route points
     *
     * @param routePoints a list of {@code GeoPosition} route points.
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setRoutePoints(List<GeoPosition> routePoints) {
        this.routePoints = routePoints;
        return this;
    }

    /**
     * Sets the maximum number of alternative routes.
     *
     * @param maxAlternatives the maximum number of alternative routes.
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setMaxAlternatives(Integer maxAlternatives) {
        this.maxAlternatives = maxAlternatives;
        return this;
    }

    /**
     * Sets the type of the alternative route
     *
     * @param alternativeType {@code AlternativeRouteType}
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setAlternativeType(AlternativeRouteType alternativeType) {
        this.alternativeType = alternativeType;
        return this;
    }

    /**
     * Sets the minimum deviation distance.
     *
     * @param minDeviationDistance the minimum deviation distance.
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setMinDeviationDistance(Integer minDeviationDistance) {
        this.minDeviationDistance = minDeviationDistance;
        return this;
    }

    /**
     * Sets the arrival time.
     *
     * @param arriveAt the arrival time.
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setArriveAt(OffsetDateTime arriveAt) {
        this.arriveAt = arriveAt;
        return this;
    }

    /**
     * Sets the minimal deviation time.
     *
     * @param minDeviationTime the minimal deviation time.
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setMinDeviationTime(Integer minDeviationTime) {
        this.minDeviationTime = minDeviationTime;
        return this;
    }

    /**
     * Sets the type of route instructions.
     *
     * @param instructionsType {@code RouteInstructionsType}
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setInstructionsType(RouteInstructionsType instructionsType) {
        this.instructionsType = instructionsType;
        return this;
    }

    /**
     * Sets the language.
     *
     * @param language Sets the language.
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions language(String language) {
        this.language = language;
        return this;
    }

    /**
     * Sets whether to use the best waypoint order.
     *
     * @param computeBestWaypointOrder whether to use the best waypoint order.
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setComputeBestWaypointOrder(Boolean computeBestWaypointOrder) {
        this.computeBestWaypointOrder = computeBestWaypointOrder;
        return this;
    }

    /**
     * Sets the representation for the best order.
     *
     * @param routeRepresentationForBestOrder {@code RouteRepresentationForBestOrder}
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setRouteRepresentationForBestOrder(RouteRepresentationForBestOrder routeRepresentationForBestOrder) {
        this.routeRepresentationForBestOrder = routeRepresentationForBestOrder;
        return this;
    }

    /**
     * Sets the type of computation for the travel time.
     *
     * @param computeTravelTime {@code ComputeTravelTime}
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setComputeTravelTime(ComputeTravelTime computeTravelTime) {
        this.computeTravelTime = computeTravelTime;
        return this;
    }

    /**
     * Sets the vehicle heading.
     *
     * @param vehicleHeading the vehicle heading.
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setVehicleHeading(Integer vehicleHeading) {
        this.vehicleHeading = vehicleHeading;
        return this;
    }

    /**
     * Sets the type of {@code Report}
     *
     * @param report the type of {@code Report}
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setReport(Report report) {
        this.report = report;
        return this;
    }

    /**
     * Sets the type of {@code SectionType} used for filtering.
     *
     * @param filterSectionType {@code SectionType}
     * @return a reference to this {@code RouteDirectionsOptions}
     */
    public RouteDirectionsOptions setFilterSectionType(SectionType filterSectionType) {
        this.filterSectionType = filterSectionType;
        return this;
    }
}
