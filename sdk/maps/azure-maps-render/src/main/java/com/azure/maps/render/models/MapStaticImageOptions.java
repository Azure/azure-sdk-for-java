// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;

import java.util.List;

/**
 * Organize inputs for get map static image
 */
@Fluent
public final class MapStaticImageOptions {
    private TilesetId tilesetId;
    private StaticMapLayer layer;
    private Integer zoom;
    private GeoPosition center;
    private GeoBoundingBox boundingBox;
    private Integer height;
    private Integer width;
    private String language;
    private LocalizedMapView localizedMapView;
    private List<String> pins;
    private List<String> path;

    public TilesetId getTilesetId() {
        return tilesetId;
    }

    public MapStaticImageOptions setTilesetId(TilesetId tilesetId) {
        this.tilesetId = tilesetId;
        return this;
    }

    /**
     * Creates an instance of {@link MapStaticImageOptions}.
     */
    public MapStaticImageOptions() {
    }

    /**
     * Gets static map layer
     *
     * @return the {@code StaticMapLayer}
     */
    public StaticMapLayer getStaticMapLayer() {
        return layer;
    }

    /**
     * Sets static map layer
     *
     * @param staticMapLayer the static map layer {@code StaticMapLayer}
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setStaticMapLayer(StaticMapLayer staticMapLayer) {
        this.layer = staticMapLayer;
        return this;
    }

    /**
     * gets zoom
     *
     * @return the zoom level
     */
    public Integer getZoom() {
        return zoom;
    }

    /**
     * sets zoom
     *
     * @param zoom the zoom level
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setZoom(Integer zoom) {
        this.zoom = zoom;
        return this;
    }

    /**
     * gets center
     *
     * @return the center of the image
     */
    public GeoPosition getCenter() {
        return center;
    }

    /**
     * sets center
     *
     * @param center center of the image
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setCenter(GeoPosition center) {
        this.center = center;
        return this;
    }

    /**
     * gets bounding box
     *
     * @return the bounding box of the image
     */
    public GeoBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * sets bounding box
     *
     * @param boundingBox the bounding box of the image
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setBoundingBox(GeoBoundingBox boundingBox) {
        this.boundingBox = boundingBox;
        return this;
    }

    /**
     * gets height
     *
     * @return the height of the image
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * sets height
     *
     * @param height the height of the image
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setHeight(Integer height) {
        this.height = height;
        return this;
    }

    /**
     * gets width
     *
     * @return the width of the image
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * sets width
     *
     * @param width the width of the image
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setWidth(Integer width) {
        this.width = width;
        return this;
    }

    /**
     * gets language
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * sets language
     *
     * @param language the language
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * gets localized map view
     *
     * @return the localized map view
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * sets localized map view
     *
     * @param localizedMapView the localized map view
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setLocalizedMapView(LocalizedMapView localizedMapView) {
        this.localizedMapView = localizedMapView;
        return this;
    }

    /**
     * gets pins
     *
     * @return a list of pins
     */
    public List<String> getPins() {
        return pins;
    }

    /**
     * sets pins
     *
     * @param pins list of pins
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setPins(List<String> pins) {
        this.pins = pins;
        return this;
    }

    /**
     * gets path
     *
     * @return a list representing the path
     */
    public List<String> getPath() {
        return path;
    }

    /**
     * sets path
     *
     * @param path the path
     * @return a reference to this {@code MapStaticImageOptions}
     */
    public MapStaticImageOptions setPath(List<String> path) {
        this.path = path;
        return this;
    }
}
