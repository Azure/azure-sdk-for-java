package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.MicrosoftLanguageTokenizer;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.MicrosoftLanguageTokenizer} and
 * {@link MicrosoftLanguageTokenizer} mismatch.
 */
public final class MicrosoftLanguageTokenizerConverter {
    public static MicrosoftLanguageTokenizer convert(com.azure.search.documents.models.MicrosoftLanguageTokenizer obj) {
        return DefaultConverter.convert(obj, MicrosoftLanguageTokenizer.class);
    }

    public static com.azure.search.documents.models.MicrosoftLanguageTokenizer convert(MicrosoftLanguageTokenizer obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.MicrosoftLanguageTokenizer.class);
    }
}
