// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.models;

import com.azure.core.models.GeoBoundingBox;

/**
 * TrafficIncidentDetailOptions class
 */
public final class TrafficIncidentDetailOptions {
    private IncidentDetailStyle style;
    private GeoBoundingBox boundingbox;
    private Integer boundingZoom;
    private String trafficmodelid;
    private String language;
    private ProjectionStandard projection;
    private IncidentGeometryType geometries;
    private Boolean expandCluster;
    private Boolean originalPosition;

    /**
     * TrafficIncidentDetailOptions constructor
     */
    public TrafficIncidentDetailOptions() {
    }

    /**
     * TrafficIncidentDetailOptions constructor
     * @param boundingBox The boundingbox is represented by two value pairs describing it's corners (first pair for lower left corner and second for upper right).
     * @param style The style that will be used to render the tile in Traffic Incident Tile API.
     * @param boundingZoom Zoom level for desired tile. 0 to 22 for raster tiles, 0 through 22 for vector tiles
     * @param trafficModelId Number referencing traffic model. This can be obtained from the Viewport API.
     */
    public TrafficIncidentDetailOptions(GeoBoundingBox boundingBox, IncidentDetailStyle style, int boundingZoom, String trafficModelId) {
        this.boundingbox = boundingBox;
        this.style = style;
        this.boundingZoom = boundingZoom;
        this.trafficmodelid = trafficModelId;
    }

    /**
     * get incident detail style
     * @return IncidentDetailStyle
     */
    public IncidentDetailStyle getIncidentDetailStyle() {
        return style;
    }

    /**
     * sets incident detail style
     * @param incidentDetailStyle The style that will be used to render the tile in Traffic Incident Tile API.
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setIncidentDetailStyle(IncidentDetailStyle incidentDetailStyle) {
        this.style = incidentDetailStyle;
        return this;
    }

    /**
     * get bounding box
     * @return GeoBoundingBox
     */
    public GeoBoundingBox getBoundingBox() {
        return boundingbox;
    }

    /**
     * sets bounding box
     * @param boundingbox The boundingbox is represented by two value pairs describing it's corners (first pair for lower left corner and second for upper right).
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setBoundingBox(GeoBoundingBox boundingbox) {
        this.boundingbox = boundingbox;
        return this;
    }
    
    /**
     * get bounding zoom
     * @return int
     */
    public int getBoundingZoom() {
        return boundingZoom;
    }

    /**
     * set bounding zoom
     * @param boundingZoom Zoom level for desired tile. 0 to 22 for raster tiles, 0 through 22 for vector tiles
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setBoundingZoom(int boundingZoom) {
        this.boundingZoom = boundingZoom;
        return this;
    }

    /**
     * get traffic model id
     * @return String
     */
    public String getTrafficIncidentDetailTrafficModelId() {
        return trafficmodelid;
    }

    /**
     * sets traffic model id
     * @param trafficmodelid Number referencing traffic model. This can be obtained from the Viewport API. 
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setTrafficmodelId(String trafficmodelid) {
        this.trafficmodelid = trafficmodelid;
        return this;
    }

    /**
     * get language
     * @return String
     */
    public String getLanguage() {
        return language;
    }

    /**
     * sets language
     * @param language ISO 639-1 code for the output language.
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * get projection standard
     * @return ProjectionStandard
     */
    public ProjectionStandard getProjectionStandard() {
        return projection;
    }

    /**
     * set projection standard
     * @param projectionStandard The projection used to specify the coordinates in the request and response. 
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setProjectionStandard(ProjectionStandard projectionStandard) {
        this.projection = projectionStandard;
        return this;
    }

    /**
     * get incident geometry type
     * @return IncidentGeometryType
     */
    public IncidentGeometryType getIncidentGeometryType() {
        return geometries;
    }

    /**
     * set incident geometry type
     * @param incidentGeometryType The type of vector geometry added to incidents (returned in the element of the response).
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setIncidentGeometryType(IncidentGeometryType incidentGeometryType) {
        this.geometries = incidentGeometryType;
        return this;
    }

    /**
     * get expand cluster
     * @return boolean
     */
    public Boolean getExpandCluster() {
        return expandCluster;
    }

    /**
     * set expand cluster
     * @param expandCluster Boolean to indicate whether to list all traffic incidents in a cluster separately
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setExpandCluster(Boolean expandCluster) {
        this.expandCluster = expandCluster;
        return this;
    }

    /**
     * get original position
     * @return boolean
     */
    public Boolean getOriginalPosition() {
        return originalPosition;
    }

    /**
     * set original position
     * @param originalPosition Boolean on whether to return the original position of the incident as well as the one shifted to the beginning of the traffic tube
     * @return TrafficIncidentDetailOptions
     */
    public TrafficIncidentDetailOptions setOriginalPosition(Boolean originalPosition) {
        this.originalPosition = originalPosition;
        return this;
    }
}
