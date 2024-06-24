// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * Map tile options class to organize inputs for get map tile
 */
@Fluent
public final class MapTileOptions {
    private TilesetId tilesetId;
    private TileIndex tileIndex;
    private OffsetDateTime timeStamp;
    private MapTileSize tileSize;
    private String language;
    private LocalizedMapView localizedMapView;

    /**
     * Creates an instance of {@link MapTileOptions}.
     */
    public MapTileOptions() {
    }

    /**
     * get tileset id
     *
     * @return tileset id
     */
    public TilesetId getTilesetId() {
        return tilesetId;
    }

    /**
     * set tileset id
     *
     * @param tilesetId the tileset id
     * @return the tileset id
     */
    public MapTileOptions setTilesetId(TilesetId tilesetId) {
        this.tilesetId = tilesetId;
        return this;
    }

    /**
     * get tile index
     *
     * @return the tile index
     */
    public TileIndex getTileIndex() {
        return tileIndex;
    }

    /**
     * set tile index
     *
     * @param tileIndex the tile index
     * @return a reference to this {@code MapTileOptions}
     */
    public MapTileOptions setTileIndex(TileIndex tileIndex) {
        this.tileIndex = tileIndex;
        return this;
    }

    /**
     * get time stamp
     *
     * @return the time stamp
     */
    public OffsetDateTime getTimestamp() {
        return timeStamp;
    }

    /**
     * set time stamp
     *
     * @param offsetDateTime the offset date time
     * @return a reference to this {@code MapTileOptions}
     */
    public MapTileOptions setTimestamp(OffsetDateTime offsetDateTime) {
        this.timeStamp = offsetDateTime;
        return this;
    }

    /**
     * get map tile size
     *
     * @return the map time size
     */
    public MapTileSize getMapTileSize() {
        return tileSize;
    }

    /**
     * set map tile size
     *
     * @param mapTileSize the map tile size
     * @return a reference to this {@code MapTileOptions}
     */
    public MapTileOptions setMapTileSize(MapTileSize mapTileSize) {
        this.tileSize = mapTileSize;
        return this;
    }

    /**
     * get language
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * set language
     *
     * @param language the language
     * @return a reference to this {@code MapTileOptions}
     */
    public MapTileOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * get localized map view
     *
     * @return the localized map view
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * set localized map view
     *
     * @param localizedMapView the localized map view
     * @return a reference to this {@code MapTileOptions}
     */
    public MapTileOptions setLocalizedMapView(LocalizedMapView localizedMapView) {
        this.localizedMapView = localizedMapView;
        return this;
    }
}
