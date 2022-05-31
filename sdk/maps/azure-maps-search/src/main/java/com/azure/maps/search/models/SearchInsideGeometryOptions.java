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
     */
    public SearchInsideGeometryOptions(String query, GeoObject geometry) {
        this.geometry = geometry;
        this.setQuery(query);
    }

    /**
     * Returns the language.
     * @return
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Returns the extended postal codes.
     * @return
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return this.extendedPostalCodesFor;
    }

    /**
     * Returns the index set.
     * @return
     */
    public List<SearchIndexes> getIndexFilter() {
        return this.indexFilter;
    }

    /**
     * Returns the geometry used for search.
     * @return
     */
    public GeoObject getGeometry() {
        return this.geometry;
    }

    /**
     * Sets the language.
     * @param language
     * @return
     */
    public SearchInsideGeometryOptions setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Sets the extended postal codes.
     * @param extendedPostalCodesFor
     * @return
     */
    public SearchInsideGeometryOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }

    /**
     * Sets the idx set.
     * @param indexFilter
     * @return
     */
    public SearchInsideGeometryOptions setIndexFilter(List<SearchIndexes> indexFilter) {
        this.indexFilter = indexFilter;
        return this;
    }

    /**
     * Sets the geometry
     * @param geometry
     * @return
     */
    public SearchInsideGeometryOptions setGeometry(GeoObject geometry) {
        this.geometry = geometry;
        return this;
    }
}
