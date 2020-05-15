package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.SplitSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.SplitSkill} and
 * {@link SplitSkill} mismatch.
 */
public final class SplitSkillConverter {
    public static SplitSkill convert(com.azure.search.documents.models.SplitSkill obj) {
        return DefaultConverter.convert(obj, SplitSkill.class);
    }

    public static com.azure.search.documents.models.SplitSkill convert(SplitSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.SplitSkill.class);
    }
}
