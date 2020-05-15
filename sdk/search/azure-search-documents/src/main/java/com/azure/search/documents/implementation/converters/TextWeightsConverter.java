package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TextWeights;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TextWeights} and
 * {@link TextWeights} mismatch.
 */
public final class TextWeightsConverter {
    public static TextWeights convert(com.azure.search.documents.models.TextWeights obj) {
        return DefaultConverter.convert(obj, TextWeights.class);
    }

    public static com.azure.search.documents.models.TextWeights convert(TextWeights obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TextWeights.class);
    }
}
