package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.MergeSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.MergeSkill} and
 * {@link MergeSkill} mismatch.
 */
public final class MergeSkillConverter {
    public static MergeSkill convert(com.azure.search.documents.models.MergeSkill obj) {
        return DefaultConverter.convert(obj, MergeSkill.class);
    }

    public static com.azure.search.documents.models.MergeSkill convert(MergeSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.MergeSkill.class);
    }
}
