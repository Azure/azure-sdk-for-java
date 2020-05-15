package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.TextTranslationSkillLanguage;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.TextTranslationSkillLanguage} and
 * {@link TextTranslationSkillLanguage} mismatch.
 */
public final class TextTranslationSkillLanguageConverter {
    public static TextTranslationSkillLanguage convert(com.azure.search.documents.models.TextTranslationSkillLanguage obj) {
        return DefaultConverter.convert(obj, TextTranslationSkillLanguage.class);
    }

    public static com.azure.search.documents.models.TextTranslationSkillLanguage convert(TextTranslationSkillLanguage obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.TextTranslationSkillLanguage.class);
    }
}
