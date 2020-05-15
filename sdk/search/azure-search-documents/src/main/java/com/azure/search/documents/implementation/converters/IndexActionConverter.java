package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexAction;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexAction} and
 * {@link IndexAction} mismatch.
 */
public final class IndexActionConverter {
    public static IndexAction convert(com.azure.search.documents.models.IndexAction obj) {
        return DefaultConverter.convert(obj, IndexAction.class);
    }

    public static com.azure.search.documents.models.IndexAction convert(IndexAction obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexAction.class);
    }
}
