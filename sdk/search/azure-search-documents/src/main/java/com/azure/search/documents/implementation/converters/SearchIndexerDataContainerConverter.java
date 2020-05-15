package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SearchIndexerDataContainer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SearchIndexerDataContainer} and
 * {@link SearchIndexerDataContainer} mismatch.
 */
public final class SearchIndexerDataContainerConverter {
    public static SearchIndexerDataContainer convert(com.azure.search.documents.models.SearchIndexerDataContainer obj) {
        return DefaultConverter.convert(obj, SearchIndexerDataContainer.class);
    }

    public static com.azure.search.documents.models.SearchIndexerDataContainer convert(SearchIndexerDataContainer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SearchIndexerDataContainer.class);
    }
}
