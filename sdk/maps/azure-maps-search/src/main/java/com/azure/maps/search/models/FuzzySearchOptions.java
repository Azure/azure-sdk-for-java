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
     * @param query
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
     * @return
     */
    public Boolean isTypeAhead() {
        return isTypeAhead;
    }

    /**
     * Returns the extended postal codes.
     * @return
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Returns the minimum fuzzy level.
     * @return
     */
    public Integer getMinFuzzyLevel() {
        return minFuzzyLevel;
    }

    /**
     * Returns the minimum fuzzy level.
     * @return
     */
    public Integer getMaxFuzzyLevel() {
        return maxFuzzyLevel;
    }

    /**
     * Returns the idx set.
     * @return
     */
    public List<SearchIndexes> getIndexFilter() {
        return indexFilter;
    }

    /**
     * Returns the entity type.
     * @return
     */
    public GeographicEntityType getEntityType() {
        return entityType;
    }

    /**
     * Returns the query string.
     * @return
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets whether this is a typeahead search.
     * @param isTypeAhead
     * @return
     */
    public FuzzySearchOptions setIsTypeAhead(Boolean isTypeAhead) {
        this.isTypeAhead = isTypeAhead;
        return this;
    }

    /**
     * Sets the extended postal codes.
     * @param extendedPostalCodesFor
     * @return
     */
    public FuzzySearchOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }

    /**
     * Sets the minimum fuzzy level.
     * @param minFuzzyLevel
     * @return
     */
    public FuzzySearchOptions setMinFuzzyLevel(Integer minFuzzyLevel) {
        this.minFuzzyLevel = minFuzzyLevel;
        return this;
    }

    /**
     * Sets the maximum fuzzy level.
     * @param maxFuzzyLevel
     * @return
     */
    public FuzzySearchOptions setMaxFuzzyLevel(Integer maxFuzzyLevel) {
        this.maxFuzzyLevel = maxFuzzyLevel;
        return this;
    }

    /**
     * Sets the idx set.
     * @param indexFilter
     * @return
     */
    public FuzzySearchOptions setIndexFilter(List<SearchIndexes> indexFilter) {
        this.indexFilter = indexFilter;
        return this;
    }

    /**
     * Sets the entity type.
     * @param entityType
     * @return
     */
    public FuzzySearchOptions setEntityType(GeographicEntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Sets the query string.
     * @param query
     * @return
     */
    public FuzzySearchOptions setQuery(String query) {
        this.query = query;
        return this;
    }
}
