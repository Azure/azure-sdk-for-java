package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.NGramTokenFilterV2;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.NGramTokenFilterV2} and
 * {@link NGramTokenFilterV2} mismatch.
 */
public final class NGramTokenFilterV2Converter {
    public static NGramTokenFilterV2 convert(com.azure.search.documents.models.NGramTokenFilterV2 obj) {
        return DefaultConverter.convert(obj, NGramTokenFilterV2.class);
    }

    public static com.azure.search.documents.models.NGramTokenFilterV2 convert(NGramTokenFilterV2 obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.NGramTokenFilterV2.class);
    }
}
