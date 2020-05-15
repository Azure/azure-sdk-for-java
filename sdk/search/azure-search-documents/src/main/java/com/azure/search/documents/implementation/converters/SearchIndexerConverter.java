package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexer} and
 * {@link SearchIndexer} mismatch.
 */
public final class SearchIndexerConverter {
    public static SearchIndexer convert(com.azure.search.documents.models.SearchIndexer obj) {
        return DefaultConverter.convert(obj, SearchIndexer.class);
    }

    public static com.azure.search.documents.models.SearchIndexer convert(SearchIndexer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexer.class);
    }
}
