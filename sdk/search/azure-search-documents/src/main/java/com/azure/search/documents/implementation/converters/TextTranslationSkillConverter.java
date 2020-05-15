package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TextTranslationSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TextTranslationSkill} and
 * {@link TextTranslationSkill} mismatch.
 */
public final class TextTranslationSkillConverter {
    public static TextTranslationSkill convert(com.azure.search.documents.models.TextTranslationSkill obj) {
        return DefaultConverter.convert(obj, TextTranslationSkill.class);
    }

    public static com.azure.search.documents.models.TextTranslationSkill convert(TextTranslationSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TextTranslationSkill.class);
    }
}
