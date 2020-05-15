package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SentimentSkillLanguage;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SentimentSkillLanguage} and
 * {@link SentimentSkillLanguage} mismatch.
 */
public final class SentimentSkillLanguageConverter {
    public static SentimentSkillLanguage convert(com.azure.search.documents.models.SentimentSkillLanguage obj) {
        return DefaultConverter.convert(obj, SentimentSkillLanguage.class);
    }

    public static com.azure.search.documents.models.SentimentSkillLanguage convert(SentimentSkillLanguage obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SentimentSkillLanguage.class);
    }
}
