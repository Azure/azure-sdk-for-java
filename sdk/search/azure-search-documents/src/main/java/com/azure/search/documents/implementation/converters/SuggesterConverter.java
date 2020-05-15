package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.Suggester;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.Suggester} and
 * {@link Suggester} mismatch.
 */
public final class SuggesterConverter {
    public static Suggester convert(com.azure.search.documents.models.Suggester obj) {
        return DefaultConverter.convert(obj, Suggester.class);
    }

    public static com.azure.search.documents.models.Suggester convert(Suggester obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.Suggester.class);
    }
}
