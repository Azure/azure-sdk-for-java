package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ShaperSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ShaperSkill} and
 * {@link ShaperSkill} mismatch.
 */
public final class ShaperSkillConverter {
    public static ShaperSkill convert(com.azure.search.documents.models.ShaperSkill obj) {
        return DefaultConverter.convert(obj, ShaperSkill.class);
    }

    public static com.azure.search.documents.models.ShaperSkill convert(ShaperSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ShaperSkill.class);
    }
}
