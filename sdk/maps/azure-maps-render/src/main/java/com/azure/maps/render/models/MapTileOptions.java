package com.azure.maps.render.models;

import java.time.OffsetDateTime;

/**
 * Map tile options class to organize inputs for get map tile
 */
public final class MapTileOptions {
    private TilesetId tilesetId;
    private TileIndex tileIndex;
    private OffsetDateTime timeStamp;
    private MapTileSize tileSize;
    private String language;
    private LocalizedMapView localizedMapView;

    /**
     * get tileset id
     */
    public TilesetId getTilesetID() {
        return tilesetId;
    }

    /**
     * set tileset id
     * @param tilesetID
     * @return
     */
    public MapTileOptions setTilesetId(TilesetId tilesetID) {
        this.tilesetId = tilesetID;
        return this;
    }

    /**
     * get tile index
     * @return
     */
    public TileIndex getTileIndex() {
        return tileIndex;
    }

    /**
     * set tile index
     * @param tileIndex
     * @return
     */
    public MapTileOptions setTileIndex(TileIndex tileIndex) {
        this.tileIndex = tileIndex;
        return this;
    }

    /**
     * get time stamp
     * @return
     */
    public OffsetDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * set time stamp
     * @param offsetDateTime
     * @return
     */
    public MapTileOptions setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.timeStamp = offsetDateTime;
        return this;
    }
    
    /**
     * get map tile size
     * @return
     */
    public MapTileSize getMapTileSize() {
        return tileSize;
    }

    /**
     * set map tile size
     * @param mapTileSize
     * @return
     */
    public MapTileOptions setMapTileSize(MapTileSize mapTileSize) {
        this.tileSize = mapTileSize;
        return this;
    }

    /**
     * get language
     * @return
     */
    public String getLanguage() {
        return language;
    }

    /**
     * set language
     * @param language
     * @return
     */
    public MapTileOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * get localized map view
     * @return
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * set localized map view
     * @param localizedMapView
     * @return
     */
    public MapTileOptions setLocalizedMapView(LocalizedMapView localizedMapView) {
        this.localizedMapView = localizedMapView;
        return this;
    }
}