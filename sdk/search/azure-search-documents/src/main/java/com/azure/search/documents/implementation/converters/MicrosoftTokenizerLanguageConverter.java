package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.MicrosoftTokenizerLanguage;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.MicrosoftTokenizerLanguage} and
 * {@link MicrosoftTokenizerLanguage} mismatch.
 */
public final class MicrosoftTokenizerLanguageConverter {
    public static MicrosoftTokenizerLanguage convert(com.azure.search.documents.models.MicrosoftTokenizerLanguage obj) {
        return DefaultConverter.convert(obj, MicrosoftTokenizerLanguage.class);
    }

    public static com.azure.search.documents.models.MicrosoftTokenizerLanguage convert(MicrosoftTokenizerLanguage obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.MicrosoftTokenizerLanguage.class);
    }
}
