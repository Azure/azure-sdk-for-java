package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.AutocompleteOptions;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.AutocompleteOptions} and
 * {@link AutocompleteOptions} mismatch.
 */
public final class AutocompleteOptionsConverter {
    public static AutocompleteOptions convert(com.azure.search.documents.models.AutocompleteOptions obj) {
        return DefaultConverter.convert(obj, AutocompleteOptions.class);
    }

    public static com.azure.search.documents.models.AutocompleteOptions convert(AutocompleteOptions obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.AutocompleteOptions.class);
    }
}
