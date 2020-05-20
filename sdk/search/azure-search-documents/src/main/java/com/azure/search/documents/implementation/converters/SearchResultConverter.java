// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.SearchResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchResult} and {@link SearchResult}.
 */
public final class SearchResultConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SearchResultConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchResult} to {@link SearchResult}.
     */
    public static SearchResult map(com.azure.search.documents.implementation.models.SearchResult obj) {
        if (obj == null) {
            return null;
        }
        SearchResult searchResult = new SearchResult();

        double _score = obj.getScore();
        PrivateFieldAccessHelper.set(searchResult, "score", _score);

        if (obj.getHighlights() != null) {
            Map<String, List<String>> _highlights =
                obj.getHighlights().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            PrivateFieldAccessHelper.set(searchResult, "highlights", _highlights);
        }

        SearchDocument _additionalProperties = new SearchDocument(obj.getAdditionalProperties());
        PrivateFieldAccessHelper.set(searchResult, "additionalProperties", _additionalProperties);
        return searchResult;
    }

    /**
     * Maps from {@link SearchResult} to {@link com.azure.search.documents.implementation.models.SearchResult}.
     */
    public static com.azure.search.documents.implementation.models.SearchResult map(SearchResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SearchResult searchResult =
            new com.azure.search.documents.implementation.models.SearchResult();

        double _score = obj.getScore();
        PrivateFieldAccessHelper.set(searchResult, "score", _score);

        if (obj.getHighlights() != null) {
            Map<String, List<String>> _highlights =
                obj.getHighlights().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            PrivateFieldAccessHelper.set(searchResult, "highlights", _highlights);
        }

        SearchDocument _additionalProperties = obj.getDocument();
        PrivateFieldAccessHelper.set(searchResult, "additionalProperties", _additionalProperties);
        return searchResult;
    }
}
