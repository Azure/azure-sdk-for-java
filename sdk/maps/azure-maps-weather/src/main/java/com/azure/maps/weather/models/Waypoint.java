// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.models;

import com.azure.core.models.GeoPosition;

/**
 * Waypoint class
 */
public final class Waypoint {
    private GeoPosition position;
    private Double estimatedTimeInMinutes = 0.0;
    private Double heading = null;

    /**
     * Waypoint Constructor
     */
    public Waypoint() {
    }

    /**
     * Waypoint constructor
     * @param position GeoPosition position in longitude, latitude
     * @param estimatedTimeInMinutes estimated time in minutes
     */
    public Waypoint(GeoPosition position, Double estimatedTimeInMinutes) {
        this.position = position;
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
    }

    /**
     * Get GeoPosition position in longitude, latitude
     * @return GeoPosition
     */
    public GeoPosition getPosition() {
        return this.position;
    }

    /**
     * Get estimated time in minutes
     * @return estimated time in minutes
     */
    public Double getEstimatedTimeInMinutes() {
        return this.estimatedTimeInMinutes;
    }

    /**
     * Get heading
     * @return heading
     */
    public Double getHeading() {
        return this.heading;
    }

    /**
     * Waypoint's position
     * @param position GeoPosition position
     * @return Waypoint
     */
    public Waypoint position(GeoPosition position) {
        this.position = position;
        return this;
    }

    /**
     * Waypoint's estimated time in minutes
     * @param estimatedTimeInMinutes estimated in time in minutes
     * @return Waypoint
     */
    public Waypoint estimatedTimeInMinutes(Double estimatedTimeInMinutes) {
        this.estimatedTimeInMinutes = estimatedTimeInMinutes;
        return this;
    }

    /**
     * Waypoint's heading
     * @param heading heading
     * @return Waypoint
     */
    public Waypoint heading(Double heading) {
        this.heading = heading;
        return this;
    }

    /**
     * To string
     */
    @Override
    public String toString() {
        String parameters = String.format("%f,%f,%f", this.position.getLatitude(),
            this.position.getLongitude(), this.estimatedTimeInMinutes);

        // heading is optional so we have to check if it's present to not break the string
        if (heading != null) {
            parameters += String.format(",%f", this.heading);
        }

        return parameters;
    }
}
