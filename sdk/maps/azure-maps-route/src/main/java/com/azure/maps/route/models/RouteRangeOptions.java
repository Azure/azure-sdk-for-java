package com.azure.maps.route.models;

import com.azure.core.models.GeoPosition;

public class RouteRangeOptions extends BaseRouteOptions<RouteRangeOptions> {
    private GeoPosition startingPoint;
    private Double fuelBudgetInLiters;
    private Double energyBudgetInKwH;
    private Double timeBudgetInSec;
    private Double distanceBudgetInMeters;

    public RouteRangeOptions(GeoPosition startingPoint, Double timeBudgetInSec) {
        this.startingPoint = startingPoint;
        this.timeBudgetInSec = timeBudgetInSec;
    }

    public GeoPosition getStartingPoint() {
        return this.startingPoint;
    }

    public Double getFuelBudgetInLiters() {
        return this.fuelBudgetInLiters;
    }

    public Double getEnergyBudgetInKwH() {
        return this.energyBudgetInKwH;
    }

    public Double getTimeBudgetInSec() {
        return this.timeBudgetInSec;
    }

    public Double getDistanceBudgetInMeters() {
        return this.distanceBudgetInMeters;
    }

    public RouteRangeOptions setStartingPoint(GeoPosition startingPoint) {
        this.startingPoint = startingPoint;
        return this;
    }

    public RouteRangeOptions setFuelBudgetInLiters(Double fuelBudgetInLiters) {
        this.fuelBudgetInLiters = fuelBudgetInLiters;
        return this;
    }

    public RouteRangeOptions setEnergyBudgetInKwH(Double energyBudgetInKwH) {
        this.energyBudgetInKwH = energyBudgetInKwH;
        return this;
    }

    public RouteRangeOptions setTimeBudgetInSec(Double timeBudgetInSec) {
        this.timeBudgetInSec = timeBudgetInSec;
        return this;
    }

    public RouteRangeOptions setDistanceBudgetInMeters(Double distanceBudgetInMeters) {
        this.distanceBudgetInMeters = distanceBudgetInMeters;
        return this;
    }


}
