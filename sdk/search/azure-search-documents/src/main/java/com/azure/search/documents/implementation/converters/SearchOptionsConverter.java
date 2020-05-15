package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchOptions;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchOptions} and
 * {@link SearchOptions} mismatch.
 */
public final class SearchOptionsConverter {
    public static SearchOptions convert(com.azure.search.documents.models.SearchOptions obj) {
        return DefaultConverter.convert(obj, SearchOptions.class);
    }

    public static com.azure.search.documents.models.SearchOptions convert(SearchOptions obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchOptions.class);
    }
}
