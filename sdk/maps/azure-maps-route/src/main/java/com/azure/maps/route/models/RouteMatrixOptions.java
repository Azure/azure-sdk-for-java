// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.models;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents the options for requesting a route matrix.
 */
public class RouteMatrixOptions {
    private RouteMatrixQuery routeMatrixQuery;
    private Boolean waitForResults;
    private ComputeTravelTime computeTravelTime;
    private SectionType filterSectionType;
    private OffsetDateTime arriveAt;
    private OffsetDateTime departAt;
    private Integer vehicleAxleWeight;
    private Double vehicleLength;
    private Double vehicleHeight;
    private Double vehicleWidth;
    private Integer vehicleMaxSpeed;
    private Integer vehicleWeight;
    private WindingnessLevel windingness;
    private InclineLevel inclineLevel;
    private TravelMode travelMode;
    private List<RouteAvoidType> avoid;
    private Boolean useTrafficData;
    private RouteType routeType;
    private VehicleLoadType vehicleLoadType;

    /**
     * Constructs a {@code RouteMatrixOptions} with the required {@link RouteMatrixQuery} parameters.
     *
     * @param routeMatrixQuery the {@code RouteMatrixQuery}
     */
    public RouteMatrixOptions(RouteMatrixQuery routeMatrixQuery) {
        this.routeMatrixQuery = routeMatrixQuery;
    }

    /**
     * Returns the query matrix used to search for a route.
     *
     * @return the {@code RouteMatrixQuery}
     */
    public RouteMatrixQuery getRouteMatrixQuery() {
        return routeMatrixQuery;
    }

    /**
     * Boolean to indicate whether to execute the request synchronously.
     *
     * @return waitForResults
     */
    public Boolean getWaitForResults() {
        return waitForResults;
    }

    /**
     * Specifies whether to return additional travel times using different types of traffic information (none, historic, live) as well as the default best-estimate travel time.
     * @return the compute travel time option
     */
    public ComputeTravelTime getComputeTravelTime() {
        return computeTravelTime;
    }

    /**
     * Specifies which of the section types is reported in the route response.
     *
     * @return the {@code SectionType}
     */
    public SectionType getFilterSectionType() {
        return filterSectionType;
    }

    /**
     * Returns the arrival time.
     *
     * @return the arrival time.
     */
    public OffsetDateTime getArriveAt() {
        return arriveAt;
    }

    /**
     * Returns the departure time.
     *
     * @return the departure time.
     */
    public OffsetDateTime getDepartAt() {
        return departAt;
    }

    /**
     * Returns the vehicle axle weight.
     *
     * @return the vehicle axle weight.
     */
    public Integer getVehicleAxleWeight() {
        return vehicleAxleWeight;
    }

    /**
     * Returns the vehicle length.
     *
     * @return the vehicle length.
     */
    public Double getVehicleLength() {
        return vehicleLength;
    }

    /**
     * Returns the vehicle height.
     *
     * @return the vehicle height
     */
    public Double getVehicleHeight() {
        return vehicleHeight;
    }

    /**
     * Returns the vehicle width
     *
     * @return the vehicle width
     */
    public Double getVehicleWidth() {
        return vehicleWidth;
    }

    /**
     * Returns the vehicle max speed.
     *
     * @return the vehicle max speed
     */
    public Integer getVehicleMaxSpeed() {
        return vehicleMaxSpeed;
    }

    /**
     * Returns the vehicle weight.
     *
     * @return the vehicle weight.
     */
    public Integer getVehicleWeight() {
        return vehicleWeight;
    }

    /**
     * Returns the windingness level
     *
     * @return the {@code WindingnessLevel}
     */
    public WindingnessLevel getWindingness() {
        return windingness;
    }

    /**
     * Returns the incline level {@code InclineLevel}
     *
     * @return the {@code InclineLevel}
     */
    public InclineLevel getInclineLevel() {
        return inclineLevel;
    }

    /**
     * Returns the travel mode {@code TravelMode}
     *
     * @return the {@code TravelMode}
     */
    public TravelMode getTravelMode() {
        return travelMode;
    }

    /**
     * Returns the list of route types to avoid.
     *
     * @return a list of {@code RouteAvoidType}
     */
    public List<RouteAvoidType> getAvoid() {
        return avoid;
    }

    /**
     * Returns whether to use the traffic data
     *
     * @return whether to use the traffic data.
     */
    public Boolean getUseTrafficData() {
        return useTrafficData;
    }

    /**
     * Returns the {@code RouteType}
     *
     * @return the {@code RouteType}
     */
    public RouteType getRouteType() {
        return routeType;
    }

    /**
     * Returns the vehicle load type
     *
     * @return the {@code VehicleLoadType}
     */
    public VehicleLoadType getVehicleLoadType() {
        return vehicleLoadType;
    }

    /**
     * Sets the {@code RouteMatrixQuery} representing the desired route matrix.
     *
     * @param routeMatrixQuery the {@code RouteMatrixQuery}
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setRouteMatrixQuery(RouteMatrixQuery routeMatrixQuery) {
        this.routeMatrixQuery = routeMatrixQuery;
        return this;
    }

    /**
     * Wait for the results of the call
     *
     * @param waitForResults wait for the results of the call
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setWaitForResults(Boolean waitForResults) {
        this.waitForResults = waitForResults;
        return this;
    }

    /**
     * Options for computing the travel time.
     *
     * @param computeTravelTime the {@code ComputeTravelTime}
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setComputeTravelTime(ComputeTravelTime computeTravelTime) {
        this.computeTravelTime = computeTravelTime;
        return this;
    }

    /**
     * Filter by certain {@code SectionType}
     *
     * @param filterSectionType the section type
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setFilterSectionType(SectionType filterSectionType) {
        this.filterSectionType = filterSectionType;
        return this;
    }

    /**
     * Sets the arrival time.
     *
     * @param arriveAt the arrival time.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setArriveAt(OffsetDateTime arriveAt) {
        this.arriveAt = arriveAt;
        return this;
    }

    /**
     * Sets the departure time.
     *
     * @param departAt the departure time.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setDepartAt(OffsetDateTime departAt) {
        this.departAt = departAt;
        return this;
    }

    /**
     * Sets the vehicle axle weight.
     *
     * @param vehicleAxleWeight the axle weight.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setVehicleAxleWeight(Integer vehicleAxleWeight) {
        this.vehicleAxleWeight = vehicleAxleWeight;
        return this;
    }

    /**
     * Sets the vehicle length.
     *
     * @param vehicleLength the vehicle length.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setVehicleLength(Double vehicleLength) {
        this.vehicleLength = vehicleLength;
        return this;
    }

    /**
     * Sets the vehicle height.
     *
     * @param vehicleHeight the vehicle height.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setVehicleHeight(Double vehicleHeight) {
        this.vehicleHeight = vehicleHeight;
        return this;
    }

    /**
     * Sets the vehicle width.
     *
     * @param vehicleWidth the vehicle width.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setVehicleWidth(Double vehicleWidth) {
        this.vehicleWidth = vehicleWidth;
        return this;
    }

    /**
     * Sets the vehicle max speed.
     *
     * @param vehicleMaxSpeed the max speed.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setVehicleMaxSpeed(Integer vehicleMaxSpeed) {
        this.vehicleMaxSpeed = vehicleMaxSpeed;
        return this;
    }

    /**
     * Sets the vehicle weight.
     *
     * @param vehicleWeight the vehicle weight.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setVehicleWeight(Integer vehicleWeight) {
        this.vehicleWeight = vehicleWeight;
        return this;
    }

    /**
     * Sets the windingness level {@code Windingness}
     *
     * @param windingness the windingness level
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setWindingness(WindingnessLevel windingness) {
        this.windingness = windingness;
        return this;
    }

    /**
     * Sets the incline level {@code InclineLevel}
     *
     * @param inclineLevel the incline level.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setInclineLevel(InclineLevel inclineLevel) {
        this.inclineLevel = inclineLevel;
        return this;
    }

    /**
     * Sets the travel mode {@code TravelMode}
     *
     * @param travelMode the travel mode.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
        return this;
    }

    /**
     * Avoid certain types of routes.
     *
     * @param avoid list of types to avoid.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setAvoid(List<RouteAvoidType> avoid) {
        this.avoid = avoid;
        return this;
    }

    /**
     * Use traffic data when calculating the range.
     *
     * @param useTrafficData use traffic data.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setUseTrafficData(Boolean useTrafficData) {
        this.useTrafficData = useTrafficData;
        return this;
    }

    /**
     * Sets the route type {@code RouteType}
     *
     * @param routeType the route type.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setRouteType(RouteType routeType) {
        this.routeType = routeType;
        return this;
    }

    /**
     * Sets the vehicle load type {@code VehicleLoadType}
     *
     * @param vehicleLoadType the vehicle load type.
     * @return a reference to this {@code RouteMatrixOptions}
     */
    public RouteMatrixOptions setVehicleLoadType(VehicleLoadType vehicleLoadType) {
        this.vehicleLoadType = vehicleLoadType;
        return this;
    }
}
