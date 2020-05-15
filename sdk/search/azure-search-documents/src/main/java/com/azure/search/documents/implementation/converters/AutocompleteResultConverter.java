package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.AutocompleteResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.AutocompleteResult} and
 * {@link AutocompleteResult} mismatch.
 */
public final class AutocompleteResultConverter {
    public static AutocompleteResult convert(com.azure.search.documents.models.AutocompleteResult obj) {
        return DefaultConverter.convert(obj, AutocompleteResult.class);
    }

    public static com.azure.search.documents.models.AutocompleteResult convert(AutocompleteResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.AutocompleteResult.class);
    }
}
