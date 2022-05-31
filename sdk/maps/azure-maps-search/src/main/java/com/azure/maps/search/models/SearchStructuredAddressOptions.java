package com.azure.maps.search.models;

import java.util.List;

/**
 * Options for searching with a StructureAddress.
 */
public final class SearchStructuredAddressOptions extends BaseSearchOptions<SearchStructuredAddressOptions> {
    private GeographicEntityType entityType;
    private List<SearchIndexes> extendedPostalCodesFor;

    /**
     * Returns the entity type.
     * @return
     */
    public GeographicEntityType getEntityType() {
        return entityType;
    }

    /**
     * Returns the extended postal codes for.
     * @return
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Sets the entity type.
     * @param entityType
     * @return
     */
    public SearchStructuredAddressOptions setEntityType(GeographicEntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Sets the extended postal codes for.
     * @param extendedPostalCodesFor
     * @return
     */
    public SearchStructuredAddressOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }
}
