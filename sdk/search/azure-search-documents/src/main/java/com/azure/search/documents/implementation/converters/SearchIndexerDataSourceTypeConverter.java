package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexerDataSourceType;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexerDataSourceType} and
 * {@link SearchIndexerDataSourceType} mismatch.
 */
public final class SearchIndexerDataSourceTypeConverter {
    public static SearchIndexerDataSourceType convert(com.azure.search.documents.models.SearchIndexerDataSourceType obj) {
        return DefaultConverter.convert(obj, SearchIndexerDataSourceType.class);
    }

    public static com.azure.search.documents.models.SearchIndexerDataSourceType convert(SearchIndexerDataSourceType obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexerDataSourceType.class);
    }
}
