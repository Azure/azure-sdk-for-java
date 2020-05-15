package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.CharFilterName;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.CharFilterName} and
 * {@link CharFilterName} mismatch.
 */
public final class CharFilterNameConverter {
    public static CharFilterName convert(com.azure.search.documents.models.CharFilterName obj) {
        return DefaultConverter.convert(obj, CharFilterName.class);
    }

    public static com.azure.search.documents.models.CharFilterName convert(CharFilterName obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.CharFilterName.class);
    }
}
