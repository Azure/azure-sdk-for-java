// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerDataContainer} and
 * {@link SearchIndexerDataContainer}.
 */
public final class SearchIndexerDataContainerConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerDataContainer} to
     * {@link SearchIndexerDataContainer}.
     */
    public static SearchIndexerDataContainer map(com.azure.search.documents.indexes.implementation.models.SearchIndexerDataContainer obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerDataContainer searchIndexerDataContainer = new SearchIndexerDataContainer();

        String query = obj.getQuery();
        searchIndexerDataContainer.setQuery(query);

        String name = obj.getName();
        searchIndexerDataContainer.setName(name);
        return searchIndexerDataContainer;
    }

    /**
     * Maps from {@link SearchIndexerDataContainer} to
     * {@link com.azure.search.documents.indexes.implementation.models.SearchIndexerDataContainer}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SearchIndexerDataContainer map(SearchIndexerDataContainer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.SearchIndexerDataContainer searchIndexerDataContainer =
            new com.azure.search.documents.indexes.implementation.models.SearchIndexerDataContainer();

        String query = obj.getQuery();
        searchIndexerDataContainer.setQuery(query);

        String name = obj.getName();
        searchIndexerDataContainer.setName(name);
        return searchIndexerDataContainer;
    }

    private SearchIndexerDataContainerConverter() {
    }
}
