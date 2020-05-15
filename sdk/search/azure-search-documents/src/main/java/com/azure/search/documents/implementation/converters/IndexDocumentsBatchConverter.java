package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexDocumentsBatch;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexDocumentsBatch} and
 * {@link IndexDocumentsBatch} mismatch.
 */
public final class IndexDocumentsBatchConverter {
    public static IndexDocumentsBatch convert(com.azure.search.documents.models.IndexDocumentsBatch obj) {
        return DefaultConverter.convert(obj, IndexDocumentsBatch.class);
    }

    public static com.azure.search.documents.models.IndexDocumentsBatch convert(IndexDocumentsBatch obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexDocumentsBatch.class);
    }
}
