package com.azure.maps.route.models;

import java.time.OffsetDateTime;
import java.util.List;

import com.azure.core.models.GeoPosition;

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

    public RouteDirectionsOptions(List<GeoPosition> routePoints) {
        this.routePoints = routePoints;
    }

    public List<GeoPosition> getRoutePoints() {
        return this.routePoints;
    }

    public Integer getMaxAlternatives() {
        return this.maxAlternatives;
    }

    public AlternativeRouteType getAlternativeType() {
        return this.alternativeType;
    }

    public Integer getMinDeviationDistance() {
        return this.minDeviationDistance;
    }

    public OffsetDateTime getArriveAt() {
        return this.arriveAt;
    }

    public Integer getMinDeviationTime() {
        return this.minDeviationTime;
    }

    public RouteInstructionsType getInstructionsType() {
        return this.instructionsType;
    }

    public String getLanguage() {
        return this.language;
    }

    public Boolean getComputeBestWaypointOrder() {
        return this.computeBestWaypointOrder;
    }

    public RouteRepresentationForBestOrder getRouteRepresentationForBestOrder() {
        return this.routeRepresentationForBestOrder;
    }

    public ComputeTravelTime getComputeTravelTime() {
        return this.computeTravelTime;
    }

    public Integer getVehicleHeading() {
        return this.vehicleHeading;
    }

    public Report getReport() {
        return this.report;
    }

    public SectionType getFilterSectionType() {
        return this.filterSectionType;
    }

    public RouteDirectionsOptions setRoutePoints(List<GeoPosition> routePoints) {
        this.routePoints = routePoints;
        return this;
    }

    public RouteDirectionsOptions setMaxAlternatives(Integer maxAlternatives) {
        this.maxAlternatives = maxAlternatives;
        return this;
    }

    public RouteDirectionsOptions setAlternativeType(AlternativeRouteType alternativeType) {
        this.alternativeType = alternativeType;
        return this;
    }

    public RouteDirectionsOptions setMinDeviationDistance(Integer minDeviationDistance) {
        this.minDeviationDistance = minDeviationDistance;
        return this;
    }

    public RouteDirectionsOptions setArriveAt(OffsetDateTime arriveAt) {
        this.arriveAt = arriveAt;
        return this;
    }

    public RouteDirectionsOptions setMinDeviationTime(Integer minDeviationTime) {
        this.minDeviationTime = minDeviationTime;
        return this;
    }

    public RouteDirectionsOptions setInstructionsType(RouteInstructionsType instructionsType) {
        this.instructionsType = instructionsType;
        return this;
    }

    public RouteDirectionsOptions language(String language) {
        this.language = language;
        return this;
    }

    public RouteDirectionsOptions setComputeBestWaypointOrder(Boolean computeBestWaypointOrder) {
        this.computeBestWaypointOrder = computeBestWaypointOrder;
        return this;
    }

    public RouteDirectionsOptions setRouteRepresentationForBestOrder(RouteRepresentationForBestOrder routeRepresentationForBestOrder) {
        this.routeRepresentationForBestOrder = routeRepresentationForBestOrder;
        return this;
    }

    public RouteDirectionsOptions setComputeTravelTime(ComputeTravelTime computeTravelTime) {
        this.computeTravelTime = computeTravelTime;
        return this;
    }

    public RouteDirectionsOptions setVehicleHeading(Integer vehicleHeading) {
        this.vehicleHeading = vehicleHeading;
        return this;
    }

    public RouteDirectionsOptions setReport(Report report) {
        this.report = report;
        return this;
    }

    public RouteDirectionsOptions setFilterSectionType(SectionType filterSectionType) {
        this.filterSectionType = filterSectionType;
        return this;
    }
}
