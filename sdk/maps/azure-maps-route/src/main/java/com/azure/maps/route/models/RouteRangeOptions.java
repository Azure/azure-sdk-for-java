// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.models;

import com.azure.core.models.GeoPosition;

/**
 * Options class for a get route range call.
 *
 */
public class RouteRangeOptions extends BaseRouteOptions<RouteRangeOptions> {
    private GeoPosition startingPoint;
    private Double fuelBudgetInLiters;
    private Double energyBudgetInKwH;
    private Double timeBudgetInSec;
    private Double distanceBudgetInMeters;

    /**
     * Builds a new {@code RouteRangeOptions}.
     *
     * @param startingPoint the starting point of the route as a {@code GeoPosition}
     * @param timeBudgetInSec the time budget of this route.
     */
    public RouteRangeOptions(GeoPosition startingPoint, Double timeBudgetInSec) {
        this.startingPoint = startingPoint;
        this.timeBudgetInSec = timeBudgetInSec;
    }

    /**
     * Returns the starting point.
     *
     * @return the starting point
     */
    public GeoPosition getStartingPoint() {
        return this.startingPoint;
    }

    /**
     * Returns the fuel budget in liters.
     *
     * @return the fuel budget.
     */
    public Double getFuelBudgetInLiters() {
        return this.fuelBudgetInLiters;
    }

    /**
     * Returns the energy budget in KwH.
     *
     * @return the energy budget
     */
    public Double getEnergyBudgetInKwH() {
        return this.energyBudgetInKwH;
    }

    /**
     * Returns the time budget in seconds.
     *
     * @return time budget in seconds.
     */
    public Double getTimeBudgetInSec() {
        return this.timeBudgetInSec;
    }

    /**
     * Returns the distance budget in meters.
     *
     * @return distance budget.
     */
    public Double getDistanceBudgetInMeters() {
        return this.distanceBudgetInMeters;
    }

    /**
     * Sets the starting point.
     *
     * @param startingPoint a {@code GeoPosition} representing the starting point of this route.
     * @return a reference to this {@code RouteRangeOptions}
     */
    public RouteRangeOptions setStartingPoint(GeoPosition startingPoint) {
        this.startingPoint = startingPoint;
        return this;
    }

    /**
     * Sets the fuel budget in liters.
     *
     * @param fuelBudgetInLiters the fuel budget.
     * @return a reference to this {@code RouteRangeOptions}
     */
    public RouteRangeOptions setFuelBudgetInLiters(Double fuelBudgetInLiters) {
        this.fuelBudgetInLiters = fuelBudgetInLiters;
        return this;
    }

    /**
     * Sets the energy budget in KwH
     *
     * @param energyBudgetInKwH the energy budget
     * @return a reference to this {@code RouteRangeOptions}
     */
    public RouteRangeOptions setEnergyBudgetInKwH(Double energyBudgetInKwH) {
        this.energyBudgetInKwH = energyBudgetInKwH;
        return this;
    }

    /**
     * Sets the time budget in seconds.
     *
     * @param timeBudgetInSec the time budget
     * @return a reference to this {@code RouteRangeOptions}
     */
    public RouteRangeOptions setTimeBudgetInSec(Double timeBudgetInSec) {
        this.timeBudgetInSec = timeBudgetInSec;
        return this;
    }

    /**
     * Sets the distance budget in meters.
     *
     * @param distanceBudgetInMeters the distance budget.
     * @return a reference to this {@code RouteRangeOptions}
     */
    public RouteRangeOptions setDistanceBudgetInMeters(Double distanceBudgetInMeters) {
        this.distanceBudgetInMeters = distanceBudgetInMeters;
        return this;
    }
}
