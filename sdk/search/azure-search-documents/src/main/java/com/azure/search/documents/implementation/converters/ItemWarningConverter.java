package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ItemWarning;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ItemWarning} and
 * {@link ItemWarning} mismatch.
 */
public final class ItemWarningConverter {
    public static ItemWarning convert(com.azure.search.documents.models.ItemWarning obj) {
        return DefaultConverter.convert(obj, ItemWarning.class);
    }

    public static com.azure.search.documents.models.ItemWarning convert(ItemWarning obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ItemWarning.class);
    }
}
