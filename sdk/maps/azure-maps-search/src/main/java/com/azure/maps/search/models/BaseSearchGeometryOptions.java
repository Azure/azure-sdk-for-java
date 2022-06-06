// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;

/**
 * Class holding optional parameters for Geometry Search.
 */
public abstract class BaseSearchGeometryOptions<T extends BaseSearchGeometryOptions<T>> {
    private String query;
    private Integer top;
    private List<Integer> categoryFilter;
    private OperatingHoursRange operatingHours;
    private LocalizedMapView localizedMapView;

    /**
     * Returns the localized map view.
     * @return the localized map view.
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * Returns the category filter.
     * @return the category filter.
     */
    public List<Integer> getCategoryFilter() {
        return categoryFilter;
    }

    /**
     * Returns the top results.
     * @return the top results.
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Returns the operating hours.
     * @return the operating hours.
     */
    public OperatingHoursRange getOperatingHours() {
        return operatingHours;
    }

    /**
     * Returns the query string.
     * @return the query string.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the category filter.
     * @param categoryFilter the category filter.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setCategoryFilter(List<Integer> categoryFilter) {
        this.categoryFilter = categoryFilter;
        return (T) this;
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
     * Sets the localized map view.
     * @param localizedMapView the localized map view.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setLocalizedMapView(LocalizedMapView localizedMapView) {
        this.localizedMapView = localizedMapView;
        return (T) this;
    }

    /**
     * Sets the operating hours.
     * @param operatingHours the operating hours.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setOperatingHours(OperatingHoursRange operatingHours) {
        this.operatingHours = operatingHours;
        return (T) this;
    }

    /**
     * Sets the query string.
     * @param query the query string.
     * @return a reference to this {@code T} class.
     */
    @SuppressWarnings("unchecked")
    public T setQuery(String query) {
        this.query = query;
        return (T) this;
    }
}
