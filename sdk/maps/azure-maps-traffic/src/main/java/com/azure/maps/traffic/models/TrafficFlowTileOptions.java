// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic.models;

/**
 * TrafficFlowTileOptions class
 */
public final class TrafficFlowTileOptions {
    private TileFormat format;
    private TrafficFlowTileStyle style;
    private Integer zoom;
    private TileIndex tileIndex;
    private Integer thickness;

    /**
     * TrafficFlowTileOptions constructor
     */
    public TrafficFlowTileOptions() {
    }

    /**
     * TrafficFlowTileOptions constructor
     * @param format Desired format of the response. Possible values are png and pbf.
     * @param style The style to be used to render the tile.
     * @param zoom Zoom level for the desired tile.
     */
    public TrafficFlowTileOptions(TileFormat format, TrafficFlowTileStyle style, int zoom) {
        this.format = format;
        this.style = style;
        this.zoom = zoom;
    }

    /**
     * gets format
     * @return TileFormat
     */
    public TileFormat getFormat() {
        return format;
    }

    /**
     * sets format
     * @param tileFormat Desired format of the response. Possible values are png and pbf.
     * @return TrafficFlowTileOptions
     */
    public TrafficFlowTileOptions setFormat(TileFormat tileFormat) {
        this.format = tileFormat;
        return this;
    }

    /**
     * gets style
     * @return TrafficFlowTileStyle
     */
    public TrafficFlowTileStyle getTrafficFlowTileStyle() {
        return style;
    }

    /**
     * sets TrafficFlowTileStyle
     * @param trafficFlowTileStyle The style to be used to render the tile.
     * @return TrafficFlowTileOptions
     */
    public TrafficFlowTileOptions setTrafficFlowTileStyle(TrafficFlowTileStyle trafficFlowTileStyle) {
        this.style = trafficFlowTileStyle;
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
     * @return TrafficFlowTileOptions
     */
    public TrafficFlowTileOptions setZoom(int zoom) {
        this.zoom = zoom;
        return this;
    }
    
    /**
     * gets tileindex
     * @return TileIndex
     */
    public TileIndex getTileIndex() {
        return tileIndex;
    }

    /**
     * sets tile index
     * @param tileIndex The tile index
     * @return TrafficFlowTileOptions
     */
    public TrafficFlowTileOptions setTileIndex(TileIndex tileIndex) {
        this.tileIndex = tileIndex;
        return this;
    }

    /**
     * gets thickness
     * @return Integer
     */
    public Integer getThickness() {
        return thickness;
    }

    /**
     * sets thickness
     * @param thickness The value of the width of the line representing traffic. This value is a multiplier and the accepted values range from 1 - 20. The default value is 10. This parameter is not valid when format is pbf.
     * @return TrafficFlowTileOptions
     */
    public TrafficFlowTileOptions setThickness(Integer thickness) {
        this.thickness = thickness;
        return this;
    }
}
