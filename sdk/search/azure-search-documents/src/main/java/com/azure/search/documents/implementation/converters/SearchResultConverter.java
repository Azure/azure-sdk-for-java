// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SearchResult} and {@link SearchResult}.
 */
public final class SearchResultConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SearchResult} to {@link SearchResult}.
     */
    public static SearchResult map(com.azure.search.documents.implementation.models.SearchResult obj,
        ObjectSerializer serializer) {
        if (obj == null) {
            return null;
        }

        SearchResult searchResult = new SearchResult(obj.getScore());

        SearchResultHelper.setRerankerScore(searchResult, obj.getRerankerScore());
        SearchResultHelper.setHighlights(searchResult, obj.getHighlights());
        SearchResultHelper.setCaptions(searchResult, obj.getCaptions());
        SearchResultHelper.setAdditionalProperties(searchResult, new SearchDocument(obj.getAdditionalProperties()));
        SearchResultHelper.setJsonSerializer(searchResult, (JsonSerializer) serializer);

        return searchResult;
    }

    private SearchResultConverter() {
    }
}
