package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndex;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndex} and
 * {@link SearchIndex} mismatch.
 */
public final class SearchIndexConverter {
    public static SearchIndex convert(com.azure.search.documents.models.SearchIndex obj) {
        return DefaultConverter.convert(obj, SearchIndex.class);
    }

    public static com.azure.search.documents.models.SearchIndex convert(SearchIndex obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndex.class);
    }
}
