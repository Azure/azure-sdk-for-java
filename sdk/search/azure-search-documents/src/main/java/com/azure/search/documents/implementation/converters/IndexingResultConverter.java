package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexingResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexingResult} and
 * {@link IndexingResult} mismatch.
 */
public final class IndexingResultConverter {
    public static IndexingResult convert(com.azure.search.documents.models.IndexingResult obj) {
        return DefaultConverter.convert(obj, IndexingResult.class);
    }

    public static com.azure.search.documents.models.IndexingResult convert(IndexingResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexingResult.class);
    }
}
