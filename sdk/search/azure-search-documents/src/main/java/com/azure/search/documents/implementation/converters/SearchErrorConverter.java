package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchError;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchError} and
 * {@link SearchError} mismatch.
 */
public final class SearchErrorConverter {
    public static SearchError convert(com.azure.search.documents.models.SearchError obj) {
        return DefaultConverter.convert(obj, SearchError.class);
    }

    public static com.azure.search.documents.models.SearchError convert(SearchError obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchError.class);
    }
}
