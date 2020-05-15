package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexerWarning;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexerWarning} and
 * {@link SearchIndexerWarning} mismatch.
 */
public final class SearchIndexerWarningConverter {
    public static SearchIndexerWarning convert(com.azure.search.documents.models.SearchIndexerWarning obj) {
        return DefaultConverter.convert(obj, SearchIndexerWarning.class);
    }

    public static com.azure.search.documents.models.SearchIndexerWarning convert(SearchIndexerWarning obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexerWarning.class);
    }
}
