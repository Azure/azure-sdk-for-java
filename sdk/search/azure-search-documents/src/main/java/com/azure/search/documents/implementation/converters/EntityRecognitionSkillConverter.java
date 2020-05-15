package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.EntityRecognitionSkill;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.EntityRecognitionSkill} and
 * {@link EntityRecognitionSkill} mismatch.
 */
public final class EntityRecognitionSkillConverter {
    public static EntityRecognitionSkill convert(com.azure.search.documents.models.EntityRecognitionSkill obj) {
        return DefaultConverter.convert(obj, EntityRecognitionSkill.class);
    }

    public static com.azure.search.documents.models.EntityRecognitionSkill convert(EntityRecognitionSkill obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.EntityRecognitionSkill.class);
    }
}
