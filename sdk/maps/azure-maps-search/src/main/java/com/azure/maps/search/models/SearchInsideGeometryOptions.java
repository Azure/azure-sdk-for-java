// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.models;

import java.util.List;

import com.azure.core.models.GeoObject;

/**
 * Class holding optional parameters for Geometry Search.
 */
public final class SearchInsideGeometryOptions extends BaseSearchGeometryOptions<SearchInsideGeometryOptions> {
    private String language;
    private GeoObject geometry;
    private List<SearchIndexes> extendedPostalCodesFor;
    private List<SearchIndexes> indexFilter;

    /**
     * Create a new options object with query and Geometry.
     * @param query query string
     * @param geometry the {@code GeoObject} geometry inside which the search will be performed.
     */
    public SearchInsideGeometryOptions(String query, GeoObject geometry) {
        this.geometry = geometry;
        this.setQuery(query);
    }

    /**
     * Returns the language.
     * @return the language
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Returns the extended postal codes.
     * @return the extended postal codes used for search
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return this.extendedPostalCodesFor;
    }

    /**
     * Returns the index set.
     * @return the index set.
     */
    public List<SearchIndexes> getIndexFilter() {
        return this.indexFilter;
    }

    /**
     * Returns the geometry used for search.
     * @return the geometry used for search.
     */
    public GeoObject getGeometry() {
        return this.geometry;
    }

    /**
     * Sets the language.
     * @param language the language.
     * @return a reference to this {@code SearchInsideGeometryOptions}
     */
    public SearchInsideGeometryOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Sets the extended postal codes.
     * @param extendedPostalCodesFor the extended postal codes used for search
     * @return a reference to this {@code SearchInsideGeometryOptions}
     */
    public SearchInsideGeometryOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }

    /**
     * Sets the index filter.
     * @param indexFilter the index filter
     * @return a reference to this {@code SearchInsideGeometryOptions}
     */
    public SearchInsideGeometryOptions setIndexFilter(List<SearchIndexes> indexFilter) {
        this.indexFilter = indexFilter;
        return this;
    }

    /**
     * Sets the geometry
     * @param geometry the geometry used for search.
     * @return a reference to this {@code SearchInsideGeometryOptions}
     */
    public SearchInsideGeometryOptions setGeometry(GeoObject geometry) {
        this.geometry = geometry;
        return this;
    }
}
