package com.azure.maps.search.models;

import java.util.List;
import java.util.Optional;

import com.azure.core.models.GeoBoundingBox;

/**
 * Class holding optional parameters for Search.
 */
public abstract class BaseSearchOptions<T extends BaseSearchOptions<T>> {
    private Integer top;
    private Integer skip;
    private List<String> countryFilter;
    private Integer radiusInMeters;
    private GeoBoundingBox boundingBox;
    private String language;
    private LocalizedMapView localizedMapView;

    /**
     * Returns the top value.
     * @return
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Returns the skip value.
     * @return
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * Returns the country filter.
     * @return
     */
    public List<String> getCountryFilter() {
        return countryFilter;
    }

    /**
     * Returns the radius in meters.
     * @return
     */
    public Integer getRadiusInMeters() {
        return radiusInMeters;
    }

    /**
     * Returns the bounding box.
     * @return
     */
    public Optional<GeoBoundingBox> getBoundingBox() {
        return Optional.ofNullable(boundingBox);
    }

    /**
     * Returns the language.
     * @return
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the localized map view.
     * @return
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * Sets the top value.
     * @param top
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setTop(Integer top) {
        this.top = top;
        return (T) this;
    }

    /**
     * Sets the skip value.
     * @param skip
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setSkip(Integer skip) {
        this.skip = skip;
        return (T) this;
    }

    /**
     * Sets the country filter.
     * @param countryFilter
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setCountryFilter(List<String> countryFilter) {
        this.countryFilter = countryFilter;
        return (T) this;
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
     * Sets the bounding box.
     * @param box
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setGeoBoundingBox(GeoBoundingBox box) {
        this.boundingBox = box;
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
}
