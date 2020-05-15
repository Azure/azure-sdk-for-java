package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.NamedEntityRecognitionSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.NamedEntityRecognitionSkill} and
 * {@link NamedEntityRecognitionSkill} mismatch.
 */
public final class NamedEntityRecognitionSkillConverter {
    public static NamedEntityRecognitionSkill convert(com.azure.search.documents.models.NamedEntityRecognitionSkill obj) {
        return DefaultConverter.convert(obj, NamedEntityRecognitionSkill.class);
    }

    public static com.azure.search.documents.models.NamedEntityRecognitionSkill convert(NamedEntityRecognitionSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.NamedEntityRecognitionSkill.class);
    }
}
