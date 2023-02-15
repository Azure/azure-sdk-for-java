// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.models;

/**
 * TrafficIncidentTileOptions class
 */
public final class TrafficIncidentTileOptions {
    private TileFormat format;
    private TrafficIncidentTileStyle style;
    private Integer zoom;
    private TileIndex tileIndex;
    private String trafficState;

    /**
     * TrafficIncidentTileOptions constructor
     */
    public TrafficIncidentTileOptions() {
    }

    /**
     * TrafficIncidentTileOptions constructor
     * @param format Desired format of the response. Possible values are png and pbf.
     * @param style The style to be used to render the tile. This parameter is not valid when format is pbf.
     * @param zoom Zoom level for the desired tile. For raster tiles, value must be in the range: 0-22 (inclusive). For vector tiles, value must be in the range: 0-22 (inclusive). 
     */
    public TrafficIncidentTileOptions(TileFormat format, TrafficIncidentTileStyle style, int zoom) {
        this.format = format;
        this.style = style;
        this.zoom = zoom;
    }

    /**
     * get format
     * @return TileFormat
     */
    public TileFormat getFormat() {
        return format;
    }

    /**
     * set format
     * @param tileFormat Desired format of the response. Possible values are png and pbf.
     * @return TrafficIncidentTileOptions
     */
    public TrafficIncidentTileOptions setFormat(TileFormat tileFormat) {
        this.format = tileFormat;
        return this;
    }

    /**
     * get traffic incident tile style
     * @return TrafficIncidentTileStyle
     */
    public TrafficIncidentTileStyle getTrafficIncidentTileStyle() {
        return style;
    }

    /**
     * set traffic incident tile style
     * @param trafficIncidentTileStyle The style to be used to render the tile. This parameter is not valid when format is pbf.
     * @return TrafficIncidentTileOptions
     */
    public TrafficIncidentTileOptions setTrafficIncidentTileStyle(TrafficIncidentTileStyle trafficIncidentTileStyle) {
        this.style = trafficIncidentTileStyle;
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
     * set zoom
     * @param zoom Zoom level for the desired tile. For raster tiles, value must be in the range: 0-22 (inclusive). For vector tiles, value must be in the range: 0-22 (inclusive). 
     * @return TrafficIncidentTileOptions
     */
    public TrafficIncidentTileOptions setZoom(int zoom) {
        this.zoom = zoom;
        return this;
    }
    
    /**
     * get tile index
     * @return TileIndex
     */
    public TileIndex getTileIndex() {
        return tileIndex;
    }

    /**
     * set tile index
     * @param tileIndex tile index
     * @return TrafficIncidentTileOptions
     */
    public TrafficIncidentTileOptions setTileIndex(TileIndex tileIndex) {
        this.tileIndex = tileIndex;
        return this;
    }

    /**
     * get traffic state
     * @return String
     */
    public String getTrafficState() {
        return trafficState;
    }

    /**
     * set traffic state
     * @param trafficState Reference value for the state of traffic at a particular time, obtained from the Viewport API call, trafficModelId attribute in trafficState field.
     * @return TrafficIncidentTileOptions
     */
    public TrafficIncidentTileOptions setTrafficState(String trafficState) {
        this.trafficState = trafficState;
        return this;
    }   
}
