package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.AsciiFoldingTokenFilter;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.AsciiFoldingTokenFilter} and
 * {@link AsciiFoldingTokenFilter} mismatch.
 */
public final class AsciiFoldingTokenFilterConverter {
    public static AsciiFoldingTokenFilter convert(com.azure.search.documents.models.AsciiFoldingTokenFilter obj) {
        return DefaultConverter.convert(obj, AsciiFoldingTokenFilter.class);
    }

    public static com.azure.search.documents.models.AsciiFoldingTokenFilter convert(AsciiFoldingTokenFilter obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.AsciiFoldingTokenFilter.class);
    }
}
