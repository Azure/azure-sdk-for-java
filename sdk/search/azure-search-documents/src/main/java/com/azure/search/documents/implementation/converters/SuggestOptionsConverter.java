package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SuggestOptions;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SuggestOptions} and
 * {@link SuggestOptions} mismatch.
 */
public final class SuggestOptionsConverter {
    public static SuggestOptions convert(com.azure.search.documents.models.SuggestOptions obj) {
        return DefaultConverter.convert(obj, SuggestOptions.class);
    }

    public static com.azure.search.documents.models.SuggestOptions convert(SuggestOptions obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SuggestOptions.class);
    }
}
