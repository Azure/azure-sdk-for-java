package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexBatch;
import com.azure.search.documents.models.IndexBatchBase;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexBatchBase} and
 * {@link IndexBatch} mismatch.
 */
public final class IndexBatchBaseConverter {
    public static <T> IndexBatch convert(IndexBatchBase<T> obj) {
        return DefaultConverter.convert(obj, IndexBatch.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> IndexBatchBase<T> convert(IndexBatch obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexBatchBase.class);
    }
}
