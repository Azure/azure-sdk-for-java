package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.models.GeoPosition;

/**
 * Options for searching point of interest category.
 */
public final class SearchPointOfInterestCategoryOptions extends BaseSearchPointOfInterestOptions<SearchPointOfInterestCategoryOptions> {
    private Boolean isTypeAhead;
    private List<SearchIndexes> extendedPostalCodesFor;
    private String query;

    /**
     * Builds fuzzy search options with query string and coordinates.
     * @param query The query to be used to search for points of interest.
     * @param coordinates The {@link GeoPosition} coordinates to be searched.
     */
    public SearchPointOfInterestCategoryOptions(String query, GeoPosition coordinates) {
        this.query = query;
        this.setCoordinates(coordinates);
    }

    /**
     * Builds fuzzy search options with query string and country filter.
     * @param query The query to be used to search for points of interest.
     * @param countryFilter A list of country codes - e.g. "US", "GB", "DE", etc.
     */
    public SearchPointOfInterestCategoryOptions(String query, List<String> countryFilter) {
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
     * Returns the extended postal codes for.
     * @return
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Returns the query string.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets whether this is a typeahead search.
     * @param isTypeAhead
     * @return
     */
    public SearchPointOfInterestCategoryOptions setIsTypeAhead(Boolean isTypeAhead) {
        this.isTypeAhead = isTypeAhead;
        return this;
    }

    /**
     * Sets the extended postal codes for.
     * @param extendedPostalCodesFor
     * @return
     */
    public SearchPointOfInterestCategoryOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }

    /**
     * Sets the query string.
     */
    public SearchPointOfInterestCategoryOptions setQuery(String query) {
        this.query = query;
        return this;
    }
}
