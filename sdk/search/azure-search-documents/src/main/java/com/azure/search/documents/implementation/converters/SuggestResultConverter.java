package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SuggestResult;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SuggestResult} and
 * {@link SuggestResult} mismatch.
 */
public final class SuggestResultConverter {
    public static SuggestResult convert(com.azure.search.documents.models.SuggestResult obj) {
        return DefaultConverter.convert(obj, SuggestResult.class);
    }

    public static com.azure.search.documents.models.SuggestResult convert(SuggestResult obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SuggestResult.class);
    }
}
