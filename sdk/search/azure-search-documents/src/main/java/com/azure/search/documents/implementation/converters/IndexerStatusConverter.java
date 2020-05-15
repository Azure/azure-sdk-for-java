package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexerStatus;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexerStatus} and
 * {@link IndexerStatus} mismatch.
 */
public final class IndexerStatusConverter {
    public static IndexerStatus convert(com.azure.search.documents.models.IndexerStatus obj) {
        return DefaultConverter.convert(obj, IndexerStatus.class);
    }

    public static com.azure.search.documents.models.IndexerStatus convert(IndexerStatus obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexerStatus.class);
    }
}
