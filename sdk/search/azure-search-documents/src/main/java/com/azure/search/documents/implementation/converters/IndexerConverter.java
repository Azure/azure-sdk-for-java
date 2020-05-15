package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.Indexer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.Indexer} and
 * {@link Indexer} mismatch.
 */
public final class IndexerConverter {
    public static Indexer convert(com.azure.search.documents.models.Indexer obj) {
        return DefaultConverter.convert(obj, Indexer.class);
    }

    public static com.azure.search.documents.models.Indexer convert(Indexer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.Indexer.class);
    }
}
