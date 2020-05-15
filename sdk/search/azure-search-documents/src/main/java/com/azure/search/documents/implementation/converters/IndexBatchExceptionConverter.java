package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexBatchException;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexBatchException} and
 * {@link IndexBatchException} mismatch.
 */
public final class IndexBatchExceptionConverter {
    public static IndexBatchException convert(com.azure.search.documents.models.IndexBatchException obj) {
        return DefaultConverter.convert(obj, IndexBatchException.class);
    }

    public static com.azure.search.documents.models.IndexBatchException convert(IndexBatchException obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexBatchException.class);
    }
}
