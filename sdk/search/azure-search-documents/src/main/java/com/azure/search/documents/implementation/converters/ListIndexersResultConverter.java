package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ListIndexersResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ListIndexersResult} and
 * {@link ListIndexersResult} mismatch.
 */
public final class ListIndexersResultConverter {
    public static ListIndexersResult convert(com.azure.search.documents.models.ListIndexersResult obj) {
        return DefaultConverter.convert(obj, ListIndexersResult.class);
    }

    public static com.azure.search.documents.models.ListIndexersResult convert(ListIndexersResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ListIndexersResult.class);
    }
}
