// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.models;

import com.azure.core.models.GeoBoundingBox;

/**
 * TrafficIncidentViewportOptions class
 */
public final class TrafficIncidentViewportOptions {
    private GeoBoundingBox boundingbox;
    private Integer boundingzoom;
    private GeoBoundingBox overviewbox;
    private Integer overviewzoom;
    private Boolean copyright;
    
    /**
     * TrafficIncidentViewportOptions constructor
     */
    public TrafficIncidentViewportOptions() {
    }

    /**
     * TrafficIncidentViewportOptions constructor
     * @param boundingBox Bounding box of the map viewport in EPSG900913 projection. The boundingbox is represented by two value pairs describing it's corners (first pair for lower left corner and second for upper right).
     * @param boundingZoom Zoom level of the map viewport. Used to determine whether the view can be zoomed in.
     * @param overviewBox Bounding box of the overview map in EPSG900913 projection.
     * @param overviewZoom Zoom level of the overview map. If there is no mini map, use the same zoom level as boundingZoom.
     */
    public TrafficIncidentViewportOptions(GeoBoundingBox boundingBox, int boundingZoom, GeoBoundingBox overviewBox, int overviewZoom) {
        this.boundingbox = boundingBox;
        this.boundingzoom = boundingZoom;
        this.overviewbox = overviewBox;
        this.overviewzoom = overviewZoom;
    }

    /**
     * get bounding box
     * @return GeoBoundingBox
     */
    public GeoBoundingBox getBoundingBox() {
        return boundingbox;
    }

    /**
     * set bounding box
     * @param boundingbox Bounding box of the map viewport in EPSG900913 projection. The boundingbox is represented by two value pairs describing it's corners (first pair for lower left corner and second for upper right). 
     * @return TrafficIncidentViewportOptions
     */
    public TrafficIncidentViewportOptions setBoundingBox(GeoBoundingBox boundingbox) {
        this.boundingbox = boundingbox;
        return this;
    }

    /**
     * get bounding zoom
     * @return int
     */
    public int getBoundingZoom() {
        return boundingzoom;
    }

    /**
     * set bounding zoom
     * @param boundingZoom Zoom level of the map viewport. Used to determine whether the view can be zoomed in.
     * @return TrafficIncidentViewportOptions
     */
    public TrafficIncidentViewportOptions setBoundingZoom(int boundingZoom) {
        this.boundingzoom = boundingZoom;
        return this;
    }

    /**
     * get overviewbox
     * @return GeoBoundingBox
     */
    public GeoBoundingBox getOverviewBox() {
        return overviewbox;
    }

    /**
     * set overview
     * @param overviewBox Bounding box of the overview map in EPSG900913 projection.
     * @return TrafficIncidentViewportOptions
     */
    public TrafficIncidentViewportOptions setOverview(GeoBoundingBox overviewBox) {
        this.overviewbox = overviewBox;
        return this;
    }

    /**
     * get overview zoom
     * @return int
     */
    public int getOverviewZoom() {
        return overviewzoom;
    }

    /**
     * set overview zoom
     * @param overviewZoom Zoom level of the overview map. If there is no mini map, use the same zoom level as boundingZoom.
     * @return TrafficIncidentViewportOptions
     */
    public TrafficIncidentViewportOptions setOverviewZoom(int overviewZoom) {
        this.overviewzoom = overviewZoom;
        return this;
    }

    /**
     * get copyright
     * @return boolean
     */
    public Boolean getCopyright() {
        return copyright;
    }

    /**
     * set copyright
     * @param copyright Determines what copyright information to return. When true the copyright text is returned; when false only the copyright index is returned.
     * @return TrafficIncidentViewportOptions
     */
    public TrafficIncidentViewportOptions setCopyright(Boolean copyright) {
        this.copyright = copyright;
        return this;
    }
}
