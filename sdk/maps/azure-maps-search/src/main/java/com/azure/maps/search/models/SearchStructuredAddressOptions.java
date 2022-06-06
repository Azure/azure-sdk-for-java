// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
     * @return the entity type
     */
    public GeographicEntityType getEntityType() {
        return entityType;
    }

    /**
     * Returns the extended postal codes for.
     * @return the extended postal codes used for the search
     */
    public List<SearchIndexes> getExtendedPostalCodesFor() {
        return extendedPostalCodesFor;
    }

    /**
     * Sets the entity type.
     * @param entityType the {@code GeographicEntityType}
     * @return a reference to this {@code SearchStructuredAddressOptions}
     */
    public SearchStructuredAddressOptions setEntityType(GeographicEntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    /**
     * Sets the extended postal codes for.
     * @param extendedPostalCodesFor the extended postal codes used for the search.
     * @return a reference to this {@code SearchStructuredAddressOptions}
     */
    public SearchStructuredAddressOptions setExtendedPostalCodesFor(List<SearchIndexes> extendedPostalCodesFor) {
        this.extendedPostalCodesFor = extendedPostalCodesFor;
        return this;
    }
}
