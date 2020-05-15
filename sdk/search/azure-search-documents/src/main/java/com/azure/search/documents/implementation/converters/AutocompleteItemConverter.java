package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.AutocompleteItem;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.AutocompleteItem} and
 * {@link AutocompleteItem} mismatch.
 */
public final class AutocompleteItemConverter {
    public static AutocompleteItem convert(com.azure.search.documents.models.AutocompleteItem obj) {
        return DefaultConverter.convert(obj, AutocompleteItem.class);
    }

    public static com.azure.search.documents.models.AutocompleteItem convert(AutocompleteItem obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.AutocompleteItem.class);
    }
}
