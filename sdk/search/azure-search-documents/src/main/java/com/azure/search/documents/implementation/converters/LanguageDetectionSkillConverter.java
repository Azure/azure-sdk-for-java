package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.LanguageDetectionSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.LanguageDetectionSkill} and
 * {@link LanguageDetectionSkill} mismatch.
 */
public final class LanguageDetectionSkillConverter {
    public static LanguageDetectionSkill convert(com.azure.search.documents.models.LanguageDetectionSkill obj) {
        return DefaultConverter.convert(obj, LanguageDetectionSkill.class);
    }

    public static com.azure.search.documents.models.LanguageDetectionSkill convert(LanguageDetectionSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.LanguageDetectionSkill.class);
    }
}
