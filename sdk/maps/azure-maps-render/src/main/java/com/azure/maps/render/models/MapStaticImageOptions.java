package com.azure.maps.render.models;

import java.util.List;

import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;

/**
 * Organize inputs for get map static image
 */
public final class MapStaticImageOptions {
    private RasterTileFormat format;
    private StaticMapLayer layer;
    private MapImageStyle style;
    private Integer zoom;
    private GeoPosition center;
    private GeoBoundingBox boundingBox;
    private Integer height;
    private Integer width;
    private String language;
    private LocalizedMapView localizedMapView;
    private List<String> pins;
    private List<String> path;

    /**
     * Gets raster tile format
     * @return
     */
    public RasterTileFormat getRasterTileFormat() {
        return format;
    }

    /**
     * Sets reaster tile format
     * @param rasterTileFormat
     * @return
     */
    public MapStaticImageOptions setRasterTileFormat(RasterTileFormat rasterTileFormat) {
        this.format = rasterTileFormat;
        return this;
    }

    /**
     * Gets static map layer
     * @return
     */
    public StaticMapLayer getStaticMapLayer() {
        return layer;
    }

    /**
     * Sets static map layer
     * @param staticMapLayer
     * @return
     */
    public MapStaticImageOptions setStaticMapLayer(StaticMapLayer staticMapLayer) {
        this.layer = staticMapLayer;
        return this;
    }

    /**
     * gets map image style
     * @return
     */
    public MapImageStyle getMapImageStyle() {
        return style;
    }

    /**
     * Sets map image style
     * @param mapImageStyle
     * @return
     */
    public MapStaticImageOptions setMapImageStyle(MapImageStyle mapImageStyle) {
        this.style = mapImageStyle;
        return this;
    }

    /**
     * gets zoom
     * @return
     */
    public Integer getZoom() {
        return zoom;
    }

    /**
     * sets zoom
     * @param zoom
     * @return
     */
    public MapStaticImageOptions setZoom(Integer zoom) {

        this.zoom = zoom;
        return this;
    }

    /**
     * gets center
     * @return
     */
    public GeoPosition getCenter() {
        return center;
    }

    /**
     * sets center
     * @param center
     * @return
     */
    public MapStaticImageOptions setCenter(GeoPosition center) {
        this.center = center;
        return this;
    }

    /**
     * gets bounding box
     * @return
     */
    public GeoBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * sets bounding box
     * @param boundingBox
     * @return
     */
    public MapStaticImageOptions setBoundingBox(GeoBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    /**
     * gets height
     * @return
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * sets height
     * @param height
     * @return
     */
    public MapStaticImageOptions setHeight(Integer height) {
        this.height = height;
        return this;
    }

    /**
     * gets width
     * @return
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * sets width
     * @param width
     * @return
     */
    public MapStaticImageOptions setWidth(Integer width) {
        this.width = width;
        return this;
    }

    /**
     * gets language
     * @return
     */
    public String getLanguage() {
        return language;
    }

    /**
     * sets language
     * @param language
     * @return
     */
    public MapStaticImageOptions setLanguage(String language) {
        this.language = language;
        return this;
    }


    /**
     * sets height
     */
    public MapStaticImageOptions setHeight(String language) {
        this.language = language;
        return this;
    }

    /**
     * gets localized map view
     * @return
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * sets localized map view
     * @param localizedMapView
     * @return
     */
    public MapStaticImageOptions setLocalizedMapView(LocalizedMapView localizedMapView) {
        this.localizedMapView = localizedMapView;
        return this;
    }

    /**
     * gets pins
     * @return
     */
    public List<String> getPins() {
        return pins;
    }

    /**
     * sets pins
     */
    public MapStaticImageOptions setPins(List<String> pins) {
        this.pins = pins;
        return this;
    }

    /**
     * gets path
     * @return
     */
    public List<String> getPath() {
        return path;
    }

    /**
     * sets path
     * @param path
     * @return
     */
    public MapStaticImageOptions setPath(List<String> path) {
        this.path = path;
        return this;
    }
}