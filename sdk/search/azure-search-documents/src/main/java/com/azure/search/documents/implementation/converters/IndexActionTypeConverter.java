package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.IndexActionType;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.IndexActionType} and
 * {@link IndexActionType} mismatch.
 */
public final class IndexActionTypeConverter {
    public static IndexActionType convert(com.azure.search.documents.models.IndexActionType obj) {
        return DefaultConverter.convert(obj, IndexActionType.class);
    }

    public static com.azure.search.documents.models.IndexActionType convert(IndexActionType obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.IndexActionType.class);
    }
}
