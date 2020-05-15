package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchMode;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchMode} and
 * {@link SearchMode} mismatch.
 */
public final class SearchModeConverter {
    public static SearchMode convert(com.azure.search.documents.models.SearchMode obj) {
        return DefaultConverter.convert(obj, SearchMode.class);
    }

    public static com.azure.search.documents.models.SearchMode convert(SearchMode obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchMode.class);
    }
}
