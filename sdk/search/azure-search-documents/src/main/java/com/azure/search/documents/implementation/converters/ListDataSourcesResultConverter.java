package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ListDataSourcesResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ListDataSourcesResult} and
 * {@link ListDataSourcesResult} mismatch.
 */
public final class ListDataSourcesResultConverter {
    public static ListDataSourcesResult convert(com.azure.search.documents.models.ListDataSourcesResult obj) {
        return DefaultConverter.convert(obj, ListDataSourcesResult.class);
    }

    public static com.azure.search.documents.models.ListDataSourcesResult convert(ListDataSourcesResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ListDataSourcesResult.class);
    }
}
