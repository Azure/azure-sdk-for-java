// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SearchIndexerDataContainer;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchIndexerDataContainer} and
 * {@link SearchIndexerDataContainer}.
 */
public final class SearchIndexerDataContainerConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchIndexerDataContainerConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchIndexerDataContainer} to
     * {@link SearchIndexerDataContainer}.
     */
    public static SearchIndexerDataContainer map(com.azure.search.documents.implementation.models.SearchIndexerDataContainer obj) {
        if (obj == null) {
            return null;
        }
        SearchIndexerDataContainer searchIndexerDataContainer = new SearchIndexerDataContainer();

        String _query = obj.getQuery();
        searchIndexerDataContainer.setQuery(_query);

        String _name = obj.getName();
        searchIndexerDataContainer.setName(_name);
        return searchIndexerDataContainer;
    }

    /**
     * Maps from {@link SearchIndexerDataContainer} to
     * {@link com.azure.search.documents.implementation.models.SearchIndexerDataContainer}.
     */
    public static com.azure.search.documents.implementation.models.SearchIndexerDataContainer map(SearchIndexerDataContainer obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchIndexerDataContainer searchIndexerDataContainer =
            new com.azure.search.documents.implementation.models.SearchIndexerDataContainer();

        String _query = obj.getQuery();
        searchIndexerDataContainer.setQuery(_query);

        String _name = obj.getName();
        searchIndexerDataContainer.setName(_name);
        return searchIndexerDataContainer;
    }
}
