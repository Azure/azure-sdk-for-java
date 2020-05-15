package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.OcrSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.OcrSkill} and
 * {@link OcrSkill} mismatch.
 */
public final class OcrSkillConverter {
    public static OcrSkill convert(com.azure.search.documents.models.OcrSkill obj) {
        return DefaultConverter.convert(obj, OcrSkill.class);
    }

    public static com.azure.search.documents.models.OcrSkill convert(OcrSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.OcrSkill.class);
    }
}
