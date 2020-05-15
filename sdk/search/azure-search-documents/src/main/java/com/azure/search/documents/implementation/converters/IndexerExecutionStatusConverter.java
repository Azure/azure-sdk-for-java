package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexerExecutionStatus;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexerExecutionStatus} and
 * {@link IndexerExecutionStatus} mismatch.
 */
public final class IndexerExecutionStatusConverter {
    public static IndexerExecutionStatus convert(com.azure.search.documents.models.IndexerExecutionStatus obj) {
        return DefaultConverter.convert(obj, IndexerExecutionStatus.class);
    }

    public static com.azure.search.documents.models.IndexerExecutionStatus convert(IndexerExecutionStatus obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexerExecutionStatus.class);
    }
}
