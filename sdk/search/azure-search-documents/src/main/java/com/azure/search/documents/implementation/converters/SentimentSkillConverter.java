package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SentimentSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SentimentSkill} and
 * {@link SentimentSkill} mismatch.
 */
public final class SentimentSkillConverter {
    public static SentimentSkill convert(com.azure.search.documents.models.SentimentSkill obj) {
        return DefaultConverter.convert(obj, SentimentSkill.class);
    }

    public static com.azure.search.documents.models.SentimentSkill convert(SentimentSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SentimentSkill.class);
    }
}
