package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.OcrSkillLanguage;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.OcrSkillLanguage} and
 * {@link OcrSkillLanguage} mismatch.
 */
public final class OcrSkillLanguageConverter {
    public static OcrSkillLanguage convert(com.azure.search.documents.models.OcrSkillLanguage obj) {
        return DefaultConverter.convert(obj, OcrSkillLanguage.class);
    }

    public static com.azure.search.documents.models.OcrSkillLanguage convert(OcrSkillLanguage obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.OcrSkillLanguage.class);
    }
}
