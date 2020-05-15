package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.Skill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.Skill} and
 * {@link Skill} mismatch.
 */
public final class SkillConverter {
    public static Skill convert(com.azure.search.documents.models.Skill obj) {
        return DefaultConverter.convert(obj, Skill.class);
    }

    public static com.azure.search.documents.models.Skill convert(Skill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.Skill.class);
    }
}
