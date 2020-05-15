package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SnowballTokenFilterLanguage;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SnowballTokenFilterLanguage} and
 * {@link SnowballTokenFilterLanguage} mismatch.
 */
public final class SnowballTokenFilterLanguageConverter {
    public static SnowballTokenFilterLanguage convert(com.azure.search.documents.models.SnowballTokenFilterLanguage obj) {
        return DefaultConverter.convert(obj, SnowballTokenFilterLanguage.class);
    }

    public static com.azure.search.documents.models.SnowballTokenFilterLanguage convert(SnowballTokenFilterLanguage obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SnowballTokenFilterLanguage.class);
    }
}
