package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.EntityRecognitionSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.EntityRecognitionSkillLanguage} and
 * {@link EntityRecognitionSkillLanguage}.
 */
public final class EntityRecognitionSkillLanguageConverter {
    private static final ClientLogger LOGGER = new ClientLogger(EntityRecognitionSkillLanguageConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.EntityRecognitionSkillLanguage} to enum
     * {@link EntityRecognitionSkillLanguage}.
     */
    public static EntityRecognitionSkillLanguage map(com.azure.search.documents.implementation.models.EntityRecognitionSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return EntityRecognitionSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link EntityRecognitionSkillLanguage} to enum
     * {@link com.azure.search.documents.implementation.models.EntityRecognitionSkillLanguage}.
     */
    public static com.azure.search.documents.implementation.models.EntityRecognitionSkillLanguage map(EntityRecognitionSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.implementation.models.EntityRecognitionSkillLanguage.fromString(obj.toString());
    }
}
