package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchResult} and
 * {@link SearchResult} mismatch.
 */
public final class SearchResultConverter {
    public static SearchResult convert(com.azure.search.documents.models.SearchResult obj) {
        return DefaultConverter.convert(obj, SearchResult.class);
    }

    public static com.azure.search.documents.models.SearchResult convert(SearchResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchResult.class);
    }
}
