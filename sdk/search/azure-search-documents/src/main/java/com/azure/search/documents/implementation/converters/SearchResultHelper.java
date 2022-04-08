// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchResult;

import java.util.List;
import java.util.Map;

/**
 * The helper class to set the non-public properties of an {@link SearchResult} instance.
 */
public final class SearchResultHelper {
    private static SearchResultAccessor accessor;

    private SearchResultHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SearchResult} instance.
     */
    public interface SearchResultAccessor {
        void setAdditionalProperties(SearchResult searchResult, SearchDocument additionalProperties);
        void setHighlights(SearchResult searchResult, Map<String, List<String>> highlights);
        void setJsonSerializer(SearchResult searchResult, JsonSerializer jsonSerializer);
    }

    /**
     * The method called from {@link SearchResult} to set it's accessor.
     *
     * @param searchResultAccessor The accessor.
     */
    public static void setAccessor(final SearchResultAccessor searchResultAccessor) {
        accessor = searchResultAccessor;
    }

    static void setAdditionalProperties(SearchResult searchResult, SearchDocument additionalProperties) {
        accessor.setAdditionalProperties(searchResult, additionalProperties);
    }

    static void setHighlights(SearchResult searchResult, Map<String, List<String>> highlights) {
        accessor.setHighlights(searchResult, highlights);
    }

    static void setJsonSerializer(SearchResult searchResult, JsonSerializer jsonSerializer) {
        accessor.setJsonSerializer(searchResult, jsonSerializer);
    }
}
