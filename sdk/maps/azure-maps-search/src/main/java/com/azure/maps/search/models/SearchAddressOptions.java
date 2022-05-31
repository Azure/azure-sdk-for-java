package com.azure.maps.search.models;

import java.util.List;
import java.util.Optional;

import com.azure.core.models.GeoPosition;

/**
 * Class holding optional parameters for Search.
 */
public final class SearchAddressOptions extends BaseSearchOptions<SearchAddressOptions> {
    private GeoPosition coordinates;
    private Boolean isTypeAhead;
    private List<SearchIndexes> extendedPostalCodesFor;
    private GeographicEntityType entityType;
    private String query;

    /**
     * Builds fuzzy search options with only the query string.
     *
     * @param query
     */
    public SearchAddressOptions(String query) {
        this.query = query;
    }

    /**
     * Returns the coordinates.
     * @return
     */
    public Optional<GeoPosition> getCoordinates() {
        return Optional.ofNullable(coordinates);
    }

    /**
     * Returns the query string.
     * @return
     */
    public String getQuery() {
        return query;
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
     * Returns the entity type.
     * @return
     */
    public GeographicEntityType getEntityType() {
        return entityType;
    }

    /**
     * Sets whether this is a typeahead search.
     * @param isTypeAhead
     * @return
     */
    public SearchAddressOptions setIsTypeAhead(Boolean isTypeAhead) {
        this.isTypeAhead = isTypeAhead;
        return this;
    }

    /**
     * Sets the extended postal codes.
     * @param extendedPostalCodesFor
     * @return
     */
    public SearchAddressOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }

    /**
     * Sets the entity type.
     * @param entityType
     * @return
     */
    public SearchAddressOptions setEntityType(GeographicEntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Sets the coordinates.
     * @param coordinates
     * @return
     */
    public SearchAddressOptions setCoordinates(GeoPosition coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    /**
     * Sets the query string.
     * @param query
     * @return
     */
    public SearchAddressOptions setQuery(String query) {
        this.query = query;
        return this;
    }
}
