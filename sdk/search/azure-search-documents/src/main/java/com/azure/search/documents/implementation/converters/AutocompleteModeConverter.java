package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.AutocompleteMode;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.AutocompleteMode} and
 * {@link AutocompleteMode} mismatch.
 */
public final class AutocompleteModeConverter {
    public static AutocompleteMode convert(com.azure.search.documents.models.AutocompleteMode obj) {
        return DefaultConverter.convert(obj, AutocompleteMode.class);
    }

    public static com.azure.search.documents.models.AutocompleteMode convert(AutocompleteMode obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.AutocompleteMode.class);
    }
}
