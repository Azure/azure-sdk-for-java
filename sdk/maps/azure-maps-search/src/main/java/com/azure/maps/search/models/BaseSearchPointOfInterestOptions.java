// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;
import java.util.Optional;

import com.azure.core.models.GeoPosition;

/**
 * Class holding optional parameters for POI Search.
 */
public abstract class BaseSearchPointOfInterestOptions<T extends BaseSearchPointOfInterestOptions<T>> extends BaseSearchOptions<T> {
    private List<Integer> categoryFilter;
    private List<String> brandFilter;
    private List<ElectricVehicleConnector> electricVehicleConnectorFilter;
    private OperatingHoursRange operatingHours;
    private GeoPosition coordinates;

    /**
     * Returns the category filter.
     * @return the category filter.
     */
    public List<Integer> getCategoryFilter() {
        return categoryFilter;
    }

    /**
     * Returns the brand filter.
     * @return the brand filter.
     */
    public List<String> getBrandFilter() {
        return brandFilter;
    }

    /**
     * Returns the electric vehicle connect filter.
     * @return the electric vehicle connect filter.
     */
    public List<ElectricVehicleConnector> getElectricVehicleConnectorFilter() {
        return electricVehicleConnectorFilter;
    }

    /**
     * Returns the operating hours.
     * @return the operating hours.
     */
    public OperatingHoursRange getOperatingHours() {
        return operatingHours;
    }

    /**
     * Returns the coordinates.
     * @return the coordinates.
     */
    public Optional<GeoPosition> getCoordinates() {
        return Optional.ofNullable(coordinates);
    }

    /**
     * Sets the category filter.
     * @param categoryFilter the category filter.
     * @return a reference to this {@code T} class
     */
    @SuppressWarnings("unchecked")
    public T setCategoryFilter(List<Integer> categoryFilter) {
        this.categoryFilter = categoryFilter;
        return (T) this;
    }

    /**
     * Sets the brand filter.
     * @param brandFilter the brand filter.
     * @return a reference to this {@code T} class
     */
    @SuppressWarnings("unchecked")
    public T setBrandFilter(List<String> brandFilter) {
        this.brandFilter = brandFilter;
        return (T) this;
    }

    /**
     * Sets the electric vehicle connector filter.
     * @param electricVehicleConnectorFilter the electric vehicle connector filter.
     * @return a reference to this {@code T} class
     */
    @SuppressWarnings("unchecked")
    public T setElectricVehicleConnectorFilter(List<ElectricVehicleConnector> electricVehicleConnectorFilter) {
        this.electricVehicleConnectorFilter = electricVehicleConnectorFilter;
        return (T) this;
    }

    /**
     * Sets the operating hours.
     * @param operatingHours the operating hours.
     * @return a reference to this {@code T} class
     */
    @SuppressWarnings("unchecked")
    public T setOperatingHours(OperatingHoursRange operatingHours) {
        this.operatingHours = operatingHours;
        return (T) this;
    }

    /**
     * Sets the coordinates.
     * @param coordinates the coordinates.
     * @return a reference to this {@code T} class
     */
    @SuppressWarnings("unchecked")
    public T setCoordinates(GeoPosition coordinates) {
        this.coordinates = coordinates;
        return (T) this;
    }
}
