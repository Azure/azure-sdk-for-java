package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexerLimits;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexerLimits} and
 * {@link IndexerLimits} mismatch.
 */
public final class IndexerLimitsConverter {
    public static IndexerLimits convert(com.azure.search.documents.models.IndexerLimits obj) {
        return DefaultConverter.convert(obj, IndexerLimits.class);
    }

    public static com.azure.search.documents.models.IndexerLimits convert(IndexerLimits obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexerLimits.class);
    }
}
