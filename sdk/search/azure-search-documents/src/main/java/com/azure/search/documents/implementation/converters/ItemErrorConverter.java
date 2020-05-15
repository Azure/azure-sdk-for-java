package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ItemError;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ItemError} and
 * {@link ItemError} mismatch.
 */
public final class ItemErrorConverter {
    public static ItemError convert(com.azure.search.documents.models.ItemError obj) {
        return DefaultConverter.convert(obj, ItemError.class);
    }

    public static com.azure.search.documents.models.ItemError convert(ItemError obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ItemError.class);
    }
}
