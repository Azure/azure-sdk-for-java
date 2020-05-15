package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexerError;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexerError} and
 * {@link SearchIndexerError} mismatch.
 */
public final class SearchIndexerErrorConverter {
    public static SearchIndexerError convert(com.azure.search.documents.models.SearchIndexerError obj) {
        return DefaultConverter.convert(obj, SearchIndexerError.class);
    }

    public static com.azure.search.documents.models.SearchIndexerError convert(SearchIndexerError obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexerError.class);
    }
}
