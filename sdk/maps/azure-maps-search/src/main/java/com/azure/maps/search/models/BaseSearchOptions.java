// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;
import java.util.Optional;

import com.azure.core.models.GeoBoundingBox;

/**
 * Class holding optional parameters for Search.
 *
 * @param <T> The extending type.
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
     * Creates a new instance of {@link BaseSearchOptions}.
     */
    public BaseSearchOptions() {
    }

    /**
     * Returns the top value.
     * @return the top value.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Returns the skip value.
     * @return the skip value.
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * Returns the country filter.
     * @return the country filter.
     */
    public List<String> getCountryFilter() {
        return countryFilter;
    }

    /**
     * Returns the radius in meters.
     * @return the radius in meters.
     */
    public Integer getRadiusInMeters() {
        return radiusInMeters;
    }

    /**
     * Returns the bounding box.
     * @return the bounding box.
     */
    public Optional<GeoBoundingBox> getBoundingBox() {
        return Optional.ofNullable(boundingBox);
    }

    /**
     * Returns the language.
     * @return the language.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Returns the localized map view.
     * @return the localized map view.
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * Sets the top value.
     * @param top the top value.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setTop(Integer top) {
        this.top = top;
        return (T) this;
    }

    /**
     * Sets the skip value.
     * @param skip the skip value.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setSkip(Integer skip) {
        this.skip = skip;
        return (T) this;
    }

    /**
     * Sets the country filter.
     * @param countryFilter the country filter.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setCountryFilter(List<String> countryFilter) {
        this.countryFilter = countryFilter;
        return (T) this;
    }

    /**
     * Sets the radius in meters.
     * @param radiusInMeters the radius in meters.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setRadiusInMeters(Integer radiusInMeters) {
        this.radiusInMeters = radiusInMeters;
        return (T) this;
    }

    /**
     * Sets the bounding box.
     * @param box the bounding box.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setGeoBoundingBox(GeoBoundingBox box) {
        this.boundingBox = box;
        return (T) this;
    }

    /**
     * Sets the language.
     * @param language the language.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setLanguage(String language) {
        this.language = language;
        return (T) this;
    }

    /**
     * Sets the localized map view.
     * @param localizedMapView the localized map view.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setLocalizedMapView(LocalizedMapView localizedMapView) {
        this.localizedMapView = localizedMapView;
        return (T) this;
    }
}
