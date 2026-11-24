// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.testingmodels;

import com.azure.search.documents.indexes.BasicField;

import java.util.ArrayList;
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
    @BasicField(
        name = "Tags",
        isSearchable = BasicField.BooleanHelper.TRUE,
        synonymMapNames = { "asynonymMaps", "", "  ", "maps" })
    public List<String> getTags() {
        return (tags == null) ? null : new ArrayList<>(tags);
    }

    /**
     * Set the tags
     *
     * @param tags The tags of hotel.
     * @return The {@link HotelWithEmptyInSynonymMaps} object itself.
     */
    public HotelWithEmptyInSynonymMaps setTags(List<String> tags) {
        this.tags = (tags == null) ? null : new ArrayList<>(tags);
        return this;
    }
}
