package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.StemmerTokenFilterLanguage;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.StemmerTokenFilterLanguage} and
 * {@link StemmerTokenFilterLanguage} mismatch.
 */
public final class StemmerTokenFilterLanguageConverter {
    public static StemmerTokenFilterLanguage convert(com.azure.search.documents.models.StemmerTokenFilterLanguage obj) {
        return DefaultConverter.convert(obj, StemmerTokenFilterLanguage.class);
    }

    public static com.azure.search.documents.models.StemmerTokenFilterLanguage convert(StemmerTokenFilterLanguage obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.StemmerTokenFilterLanguage.class);
    }
}
