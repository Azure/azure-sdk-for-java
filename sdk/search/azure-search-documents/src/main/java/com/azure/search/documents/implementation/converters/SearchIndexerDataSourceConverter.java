package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexerDataSource;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexerDataSource} and
 * {@link SearchIndexerDataSource} mismatch.
 */
public final class SearchIndexerDataSourceConverter {
    public static SearchIndexerDataSource convert(com.azure.search.documents.models.SearchIndexerDataSource obj) {
        return DefaultConverter.convert(obj, SearchIndexerDataSource.class);
    }

    public static com.azure.search.documents.models.SearchIndexerDataSource convert(SearchIndexerDataSource obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexerDataSource.class);
    }
}
