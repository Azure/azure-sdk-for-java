package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.ConditionalSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.ConditionalSkill} and
 * {@link ConditionalSkill} mismatch.
 */
public final class ConditionalSkillConverter {
    public static ConditionalSkill convert(com.azure.search.documents.models.ConditionalSkill obj) {
        return DefaultConverter.convert(obj, ConditionalSkill.class);
    }

    public static com.azure.search.documents.models.ConditionalSkill convert(ConditionalSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.ConditionalSkill.class);
    }
}
