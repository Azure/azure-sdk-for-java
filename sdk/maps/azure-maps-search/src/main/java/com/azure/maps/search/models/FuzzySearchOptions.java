// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.models.GeoPosition;

/**
 * Class holding parameters for a fuzzy search.
 */
public final class FuzzySearchOptions extends BaseSearchPointOfInterestOptions<FuzzySearchOptions> {
    private Boolean isTypeAhead;
    private List<SearchIndexes> extendedPostalCodesFor;
    private Integer minFuzzyLevel;
    private Integer maxFuzzyLevel;
    private List<SearchIndexes> indexFilter;
    private GeographicEntityType entityType;
    private String query;

    /**
     * Builds fuzzy search options with only the query string.
     *
     * @param query the query string used in the search.
     */
    public FuzzySearchOptions(String query) {
        this.query = query;
    }

    /**
     * Builds fuzzy search options with query string and coordinates.
     * @param query The query to be used to search for points of interest.
     * @param coordinates The {@link GeoPosition} coordinates to be searched.
     */
    public FuzzySearchOptions(String query, GeoPosition coordinates) {
        this.query = query;
        this.setCoordinates(coordinates);
    }

    /**
     * Builds fuzzy search options with query string and country filter.
     * @param query The query to be used to search for points of interest.
     * @param countryFilter A list of country codes - e.g. "US", "GB", "DE", etc.
     */
    public FuzzySearchOptions(String query, List<String> countryFilter) {
        this.query = query;
        this.setCountryFilter(countryFilter);
    }

    /**
     * Returns whether this is a typeahead search.
     * @return whether this is a typeahead search.
     */
    public Boolean isTypeAhead() {
        return isTypeAhead;
    }

    /**
     * Returns the extended postal codes.
     * @return the extended postal codes used for the search.
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Returns the minimum fuzzy level.
     * @return the minimum fuzzy level.
     */
    public Integer getMinFuzzyLevel() {
        return minFuzzyLevel;
    }

    /**
     * Returns the maximum fuzzy level.
     * @return the maximum fuzzy level.
     */
    public Integer getMaxFuzzyLevel() {
        return maxFuzzyLevel;
    }

    /**
     * Returns the index filters used in the search.
     * @return the index filters used in the search.
     */
    public List<SearchIndexes> getIndexFilter() {
        return indexFilter;
    }

    /**
     * Returns the entity type.
     * @return the entity type.
     */
    public GeographicEntityType getEntityType() {
        return entityType;
    }

    /**
     * Returns the query string.
     * @return the query string.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets whether this is a typeahead search.
     * @param isTypeAhead the extended postal codes.
     * @return a reference to this {@code FuzzySearchOptions}
     */
    public FuzzySearchOptions setIsTypeAhead(Boolean isTypeAhead) {
        this.isTypeAhead = isTypeAhead;
        return this;
    }

    /**
     * Sets the extended postal codes.
     * @param extendedPostalCodesFor the extended postal codes used for the search.
     * @return a reference to this {@code FuzzySearchOptions}
     */
    public FuzzySearchOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }

    /**
     * Sets the minimum fuzzy level.
     * @param minFuzzyLevel the minimum fuzzy level.
     * @return a reference to this {@code FuzzySearchOptions}
     */
    public FuzzySearchOptions setMinFuzzyLevel(Integer minFuzzyLevel) {
        this.minFuzzyLevel = minFuzzyLevel;
        return this;
    }

    /**
     * Sets the maximum fuzzy level.
     * @param maxFuzzyLevel the maximum fuzzy level.
     * @return a reference to this {@code FuzzySearchOptions}
     */
    public FuzzySearchOptions setMaxFuzzyLevel(Integer maxFuzzyLevel) {
        this.maxFuzzyLevel = maxFuzzyLevel;
        return this;
    }

    /**
     * Sets the index filters used in the search.
     * @param indexFilter the index filters used in the search.
     * @return a reference to this {@code FuzzySearchOptions}
     */
    public FuzzySearchOptions setIndexFilter(List<SearchIndexes> indexFilter) {
        this.indexFilter = indexFilter;
        return this;
    }

    /**
     * Sets the entity type.
     * @param entityType the entity type.
     * @return a reference to this {@code FuzzySearchOptions}
     */
    public FuzzySearchOptions setEntityType(GeographicEntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Sets the query string.
     * @param query the query string.
     * @return a reference to this {@code FuzzySearchOptions}
     */
    public FuzzySearchOptions setQuery(String query) {
        this.query = query;
        return this;
    }
}
