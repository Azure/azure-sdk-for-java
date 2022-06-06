// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.models.GeoPosition;

/**
 * Options for searching points of interest.
 */
public final class SearchPointOfInterestOptions extends BaseSearchPointOfInterestOptions<SearchPointOfInterestOptions> {
    private Boolean isTypeAhead;
    private List<PointOfInterestExtendedPostalCodes> extendedPostalCodesFor;
    private String query;

    /**
     * Builds fuzzy search options with query string and coordinates.
     * @param query The query to be used to search for points of interest.
     * @param coordinates The {@link GeoPosition} coordinates to be searched.
     */
    public SearchPointOfInterestOptions(String query, GeoPosition coordinates) {
        this.query = query;
        this.setCoordinates(coordinates);
    }

    /**
     * Builds fuzzy search options with query string and country filter.
     * @param query The query to be used to search for points of interest.
     * @param countryFilter A list of country codes - e.g. "US", "GB", "DE", etc.
     */
    public SearchPointOfInterestOptions(String query, List<String> countryFilter) {
        this.query = query;
        this.setCountryFilter(countryFilter);
    }

    /**
     * Returns whether this is a typeahead search.
     * @return whether this is a typeahead search
     */
    public Boolean isTypeAhead() {
        return isTypeAhead;
    }

    /**
     * Returns the extended postal codes for.
     * @return the extended postal codes used for the search
     */
    public List<PointOfInterestExtendedPostalCodes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Returns the query string.
     * @return the query string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets whether this is a typeahead search.
     * @param isTypeAhead whether this is a typeahead search.
     * @return a reference to this {@code SearchPointOfInterestOptions}
     */
    public SearchPointOfInterestOptions isTypeAhead(Boolean isTypeAhead) {
        this.isTypeAhead = isTypeAhead;
        return this;
    }

    /**
     * Sets the extended postal codes for.
     * @param extendedPostalCodesFor extended postal codes used for search
     * @return a reference to this {@code SearchPointOfInterestOptions}
     */
    public SearchPointOfInterestOptions setExtendedPostalCodesFor(List<PointOfInterestExtendedPostalCodes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }

    /**
     * Sets the query string.
     * @param query the query string
     * @return a reference to this {@code SearchPointOfInterestOptions}
     */
    public SearchPointOfInterestOptions setQuery(String query) {
        this.query = query;
        return this;
    }
}
