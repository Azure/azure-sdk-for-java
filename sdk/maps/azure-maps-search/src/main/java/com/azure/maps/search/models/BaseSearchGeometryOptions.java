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
     * @return
     */
    public LocalizedMapView getLocalizedMapView() {
        return localizedMapView;
    }

    /**
     * Returns the category filter.
     * @return
     */
    public List<Integer> getCategoryFilter() {
        return categoryFilter;
    }

    /**
     * Returns the top results.
     * @return
     */
    public Integer getTop() {
        return top;
    }

    /**
     * Returns the operating hours.
     * @return
     */
    public OperatingHoursRange getOperatingHours() {
        return operatingHours;
    }

    /**
     * Returns the query string.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the category filter.
     * @param categoryFilter
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setCategoryFilter(List<Integer> categoryFilter) {
        this.categoryFilter = categoryFilter;
        return (T) this;
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
     * Sets the operating hours.
     * @param operatingHours
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setOperatingHours(OperatingHoursRange operatingHours) {
        this.operatingHours = operatingHours;
        return (T) this;
    }

    /**
     * Sets the query string.
     * @param query
     * @return
     */
    @SuppressWarnings("unchecked")
    public T setQuery(String query) {
        this.query = query;
        return (T) this;
    }
}
