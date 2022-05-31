package com.azure.maps.search.models;

import com.azure.core.models.GeoPosition;

/**
 * Class holding optional parameters for reverse search.
 */
public abstract class BaseReverseSearchOptions<T extends BaseReverseSearchOptions<T>> {
    private String language;
    private Integer heading;
    private Integer radiusInMeters;
    private LocalizedMapView localizedMapView;
    private GeoPosition coordinates;

    /**
     * Returns an Integer with the radius, in meters.
     * @return the radius in meters.
     */
    public Integer getRadiusInMeters() {
        return radiusInMeters;
    }

    /**
     * Returns a String with the chosen language.
     * @return the language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns a LocalizedMapView representing the geopolitical view of the map.
     * @return the localized map view.
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * Returns an Integer representing the heading.
     * @return the heading.
     */
    public Integer getHeading() {
        return heading;
    }

    /**
     * Returns a GeoPosition with the requested coordinates.
     * @return the coordinates to be used in the search.
     */
    public GeoPosition getCoordinates() {
        return this.coordinates;
    }

    /**
     * Sets the radius in meters.
     * @param radiusInMeters
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setRadiusInMeters(Integer radiusInMeters) {
        this.radiusInMeters = radiusInMeters;
        return (T) this;
    }

    /**
     * Sets the heading.
     * @param heading
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setHeading(Integer heading) {
        this.heading = heading;
        return (T) this;
    }

    /**
     * Sets the language.
     * @param language
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setLanguage(String language) {
        this.language = language;
        return (T) this;
    }

    /**
     * Sets the localized map view.
     * @param localizedMapView
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setLocalizedMapView(LocalizedMapView localizedMapView) {
        this.localizedMapView = localizedMapView;
        return (T) this;
    }

    /**
     * Sets the coordinates.
     * @param coordinates
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setCoordinates(GeoPosition coordinates) {
        this.coordinates = coordinates;
        return (T) this;
    }
}
