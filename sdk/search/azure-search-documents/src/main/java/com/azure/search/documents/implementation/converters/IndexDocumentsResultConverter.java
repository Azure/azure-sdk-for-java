package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexDocumentsResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexDocumentsResult} and
 * {@link IndexDocumentsResult} mismatch.
 */
public final class IndexDocumentsResultConverter {
    public static IndexDocumentsResult convert(com.azure.search.documents.models.IndexDocumentsResult obj) {
        return DefaultConverter.convert(obj, IndexDocumentsResult.class);
    }

    public static com.azure.search.documents.models.IndexDocumentsResult convert(IndexDocumentsResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexDocumentsResult.class);
    }
}
