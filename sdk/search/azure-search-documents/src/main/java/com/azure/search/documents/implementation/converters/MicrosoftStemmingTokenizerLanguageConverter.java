package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.MicrosoftStemmingTokenizerLanguage;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.MicrosoftStemmingTokenizerLanguage} and
 * {@link MicrosoftStemmingTokenizerLanguage} mismatch.
 */
public final class MicrosoftStemmingTokenizerLanguageConverter {
    public static MicrosoftStemmingTokenizerLanguage convert(com.azure.search.documents.models.MicrosoftStemmingTokenizerLanguage obj) {
        return DefaultConverter.convert(obj, MicrosoftStemmingTokenizerLanguage.class);
    }

    public static com.azure.search.documents.models.MicrosoftStemmingTokenizerLanguage convert(MicrosoftStemmingTokenizerLanguage obj) {
        return DefaultConverter.convert(obj,
            com.azure.search.documents.models.MicrosoftStemmingTokenizerLanguage.class);
    }
}
