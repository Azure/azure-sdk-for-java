// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test.environment.models;

import com.azure.search.documents.indexes.SearchableFieldProperty;

import java.util.List;

/**
 * This is a class to test whether we filter out the empty String in synonymMaps.
 */
public class HotelWithEmptyInSynonymMaps {
    private List<String> tags;

    /**
     * Gets the tags.
     *
     * @return The tags of hotel.
     */
    @SearchableFieldProperty(synonymMapNames = {"asynonymMaps", "", "  ", "maps"})
    public List<String> getTags() {
        return tags;
    }

    /**
     * Set the tags
     *
     * @param tags The tags of hotel.
     * @return The {@link HotelWithEmptyInSynonymMaps} object itself.
     */
    public HotelWithEmptyInSynonymMaps setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }
}
