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
     * @param routeMatrixQuery
     */
    public RouteMatrixOptions(RouteMatrixQuery routeMatrixQuery) {
        this.routeMatrixQuery = routeMatrixQuery;
    }

    /**
     * Returns the query matrix used to search for a route.
     *
     * @return the @{RouteMatrixQuery}
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
     * @return
     */
    public SectionType getFilterSectionType() {
        return filterSectionType;
    }

    public OffsetDateTime getArriveAt() {
        return arriveAt;
    }

    public OffsetDateTime getDepartAt() {
        return departAt;
    }

    public Integer getVehicleAxleWeight() {
        return vehicleAxleWeight;
    }

    public Double getVehicleLength() {
        return vehicleLength;
    }

    public Double getVehicleHeight() {
        return vehicleHeight;
    }

    public Double getVehicleWidth() {
        return vehicleWidth;
    }

    public Integer getVehicleMaxSpeed() {
        return vehicleMaxSpeed;
    }

    public Integer getVehicleWeight() {
        return vehicleWeight;
    }

    public WindingnessLevel getWindingness() {
        return windingness;
    }

    public InclineLevel getInclineLevel() {
        return inclineLevel;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public List<RouteAvoidType> getAvoid() {
        return avoid;
    }

    public Boolean getUseTrafficData() {
        return useTrafficData;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public VehicleLoadType getVehicleLoadType() {
        return vehicleLoadType;
    }

    public RouteMatrixOptions setRouteMatrixQuery(RouteMatrixQuery routeMatrixQuery) {
        this.routeMatrixQuery = routeMatrixQuery;
        return this;
    }

    public RouteMatrixOptions setWaitForResults(Boolean waitForResults) {
        this.waitForResults = waitForResults;
        return this;
    }

    public RouteMatrixOptions setComputeTravelTime(ComputeTravelTime computeTravelTime) {
        this.computeTravelTime = computeTravelTime;
        return this;
    }

    public RouteMatrixOptions setFilterSectionType(SectionType filterSectionType) {
        this.filterSectionType = filterSectionType;
        return this;
    }

    public RouteMatrixOptions setArriveAt(OffsetDateTime arriveAt) {
        this.arriveAt = arriveAt;
        return this;
    }

    public RouteMatrixOptions setDepartAt(OffsetDateTime departAt) {
        this.departAt = departAt;
        return this;
    }

    public RouteMatrixOptions setVehicleAxleWeight(Integer vehicleAxleWeight) {
        this.vehicleAxleWeight = vehicleAxleWeight;
        return this;
    }

    public RouteMatrixOptions setVehicleLength(Double vehicleLength) {
        this.vehicleLength = vehicleLength;
        return this;
    }

    public RouteMatrixOptions setVehicleHeight(Double vehicleHeight) {
        this.vehicleHeight = vehicleHeight;
        return this;
    }

    public RouteMatrixOptions setVehicleWidth(Double vehicleWidth) {
        this.vehicleWidth = vehicleWidth;
        return this;
    }

    public RouteMatrixOptions setVehicleMaxSpeed(Integer vehicleMaxSpeed) {
        this.vehicleMaxSpeed = vehicleMaxSpeed;
        return this;
    }

    public RouteMatrixOptions setVehicleWeight(Integer vehicleWeight) {
        this.vehicleWeight = vehicleWeight;
        return this;
    }

    public RouteMatrixOptions setWindingness(WindingnessLevel windingness) {
        this.windingness = windingness;
        return this;
    }

    public RouteMatrixOptions setInclineLevel(InclineLevel inclineLevel) {
        this.inclineLevel = inclineLevel;
        return this;
    }

    public RouteMatrixOptions setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
        return this;
    }

    public RouteMatrixOptions setAvoid(List<RouteAvoidType> avoid) {
        this.avoid = avoid;
        return this;
    }

    public RouteMatrixOptions setUseTrafficData(Boolean useTrafficData) {
        this.useTrafficData = useTrafficData;
        return this;
    }

    public RouteMatrixOptions setRouteType(RouteType routeType) {
        this.routeType = routeType;
        return this;
    }

    public RouteMatrixOptions setVehicleLoadType(VehicleLoadType vehicleLoadType) {
        this.vehicleLoadType = vehicleLoadType;
        return this;
    }
}
