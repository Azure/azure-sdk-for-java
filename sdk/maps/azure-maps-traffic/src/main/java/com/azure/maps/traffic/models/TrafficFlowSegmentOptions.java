// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.models;

import com.azure.core.models.GeoPosition;

/**
 * TrafficFlowSegmentOptions class
 */
public final class TrafficFlowSegmentOptions {
    private TrafficFlowSegmentStyle style;
    private Integer zoom;
    private GeoPosition coordinates;
    private SpeedUnit unit;
    private Integer thickness;
    private Boolean openLr;

    /**
     * Constructor for TrafficFlowSegmentOptions
     */
    public TrafficFlowSegmentOptions() {
    }

    /**
     * Constructor for TrafficFlowSegmentOptions
     * @param style The style to be used to render the tile.
     * @param zoom Zoom level for the desired tile.
     * @param coordinates Coordinates of the point close to the road segment. This parameter is a list of four coordinates, containing two coordinate pairs (lat, long, lat, long), and calculated using EPSG4326 projection.
     */
    public TrafficFlowSegmentOptions(TrafficFlowSegmentStyle style, int zoom, GeoPosition coordinates) {
        this.style = style;
        this.zoom = zoom;
        this.coordinates = coordinates;
    }

    /**
     * get TrafficFlowSegmentStyle
     * @return TrafficFlowSegmentStyle
     */
    public TrafficFlowSegmentStyle getTrafficFlowSegmentStyle() {
        return style;
    }
 
    /**
     * Sets TrafficFlowTileStyle
     * @param trafficFlowSegmentStyle The style to be used to render the tile.
     * @return TrafficFlowSegmentOptions
     */
    public TrafficFlowSegmentOptions setTrafficFlowSegmentStyle(TrafficFlowSegmentStyle trafficFlowSegmentStyle) {
        this.style = trafficFlowSegmentStyle;
        return this;
    }

    /**
     * get zoom
     * @return int
     */
    public int getZoom() {
        return zoom;
    }

    /**
     * sets zoom
     * @param zoom Zoom level for the desired tile.
     * @return TrafficFlowSegmentOptions
     */
    public TrafficFlowSegmentOptions setZoom(int zoom) {
        this.zoom = zoom;
        return this;
    }
    
    /**
     * get coordinates
     * @return GeoPosition
     */
    public GeoPosition getCoordinates() {
        return coordinates;
    }

    /**
     * sets coordinates
     * @param coordinates Coordinates of the point close to the road segment. This parameter is a list of four coordinates, containing two coordinate pairs (lat, long, lat, long), and calculated using EPSG4326 projection.
     * @return TrafficFlowSegmentOptions
     */
    public TrafficFlowSegmentOptions setCoordinates(GeoPosition coordinates) {
        this.coordinates = coordinates;
        return this;
    }
 
    /**
     * gets unit
     * @return SpeedUnit
     */
    public SpeedUnit getUnit() {
        return unit;
    }

    /**
     * sets unit
     * @param unit Unit of speed in KMPH or MPH
     * @return TrafficFlowSegmentOptions
     */
    public TrafficFlowSegmentOptions setUnit(SpeedUnit unit) {
        this.unit = unit;
        return this;
    }

    /**
     * gets thickness
     * @return integer
     */
    public Integer getThickness() {
        return thickness;
    }

    /**
     * sets thickness
     * @param thickness The value of the width of the line representing traffic. 
     * @return TrafficFlowSegmentOptions
     */
    public TrafficFlowSegmentOptions setThickness(Integer thickness) {
        this.thickness = thickness;
        return this;
    }

    /**
     * gets openLr
     * @return boolean
     */
    public Boolean getOpenLr() {
        return openLr;
    }

    /**
     * sets openLr
     * @param openLr Boolean on whether the response should include OpenLR code
     * @return TrafficFlowSegmentOptions
     */
    public TrafficFlowSegmentOptions setOpenLr(Boolean openLr) {
        this.openLr = openLr;
        return this;
    }
}
