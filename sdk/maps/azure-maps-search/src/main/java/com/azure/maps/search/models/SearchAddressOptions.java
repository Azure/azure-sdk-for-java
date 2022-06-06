// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
     * @param query the query string used in the fuzzy search.
     */
    public SearchAddressOptions(String query) {
        this.query = query;
    }

    /**
     * Returns the coordinates.
     * @return the coordinates.
     */
    public Optional<GeoPosition> getCoordinates() {
        return Optional.ofNullable(coordinates);
    }

    /**
     * Returns the query string.
     * @return the query string.
     */
    public String getQuery() {
        return query;
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
     * @return the extended postal codes used in the search.
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Returns the entity type.
     * @return the entity type.
     */
    public GeographicEntityType getEntityType() {
        return entityType;
    }

    /**
     * Sets whether this is a typeahead search.
     * @param isTypeAhead whether this is a typeahead search.
     * @return a reference to this @{code SearchAddressOptions}
     */
    public SearchAddressOptions setIsTypeAhead(Boolean isTypeAhead) {
        this.isTypeAhead = isTypeAhead;
        return this;
    }

    /**
     * Sets the extended postal codes.
     * @param extendedPostalCodesFor the extended postal codes used in the search.
     * @return a reference to this @{code SearchAddressOptions}
     */
    public SearchAddressOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }

    /**
     * Sets the entity type.
     * @param entityType the entity type.
     * @return a reference to this @{code SearchAddressOptions}
     */
    public SearchAddressOptions setEntityType(GeographicEntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Sets the coordinates.
     * @param coordinates the coordinates.
     * @return a reference to this @{code SearchAddressOptions}
     */
    public SearchAddressOptions setCoordinates(GeoPosition coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    /**
     * Sets the query string.
     * @param query the query string.
     * @return a reference to this @{code SearchAddressOptions}
     */
    public SearchAddressOptions setQuery(String query) {
        this.query = query;
        return this;
    }
}
