package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexerLimits;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexerLimits} and
 * {@link SearchIndexerLimits} mismatch.
 */
public final class SearchIndexerLimitsConverter {
    public static SearchIndexerLimits convert(com.azure.search.documents.models.SearchIndexerLimits obj) {
        return DefaultConverter.convert(obj, SearchIndexerLimits.class);
    }

    public static com.azure.search.documents.models.SearchIndexerLimits convert(SearchIndexerLimits obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexerLimits.class);
    }
}
