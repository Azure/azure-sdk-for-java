package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexerExecutionResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexerExecutionResult} and
 * {@link IndexerExecutionResult} mismatch.
 */
public final class IndexerExecutionResultConverter {
    public static IndexerExecutionResult convert(com.azure.search.documents.models.IndexerExecutionResult obj) {
        return DefaultConverter.convert(obj, IndexerExecutionResult.class);
    }

    public static com.azure.search.documents.models.IndexerExecutionResult convert(IndexerExecutionResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexerExecutionResult.class);
    }
}
