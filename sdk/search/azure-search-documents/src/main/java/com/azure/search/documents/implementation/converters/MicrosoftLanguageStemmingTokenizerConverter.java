package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.MicrosoftLanguageStemmingTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.MicrosoftLanguageStemmingTokenizer} and
 * {@link MicrosoftLanguageStemmingTokenizer} mismatch.
 */
public final class MicrosoftLanguageStemmingTokenizerConverter {
    public static MicrosoftLanguageStemmingTokenizer convert(com.azure.search.documents.models.MicrosoftLanguageStemmingTokenizer obj) {
        return DefaultConverter.convert(obj, MicrosoftLanguageStemmingTokenizer.class);
    }

    public static com.azure.search.documents.models.MicrosoftLanguageStemmingTokenizer convert(MicrosoftLanguageStemmingTokenizer obj) {
        return DefaultConverter.convert(obj,
            com.azure.search.documents.models.MicrosoftLanguageStemmingTokenizer.class);
    }
}
