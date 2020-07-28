// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.EntityRecognitionSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillLanguage} and
 * {@link EntityRecognitionSkillLanguage}.
 */
public final class EntityRecognitionSkillLanguageConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillLanguage} to enum
     * {@link EntityRecognitionSkillLanguage}.
     */
    public static EntityRecognitionSkillLanguage map(com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return EntityRecognitionSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link EntityRecognitionSkillLanguage} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillLanguage}.
     */
    public static com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillLanguage map(EntityRecognitionSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillLanguage.fromString(obj.toString());
    }

    private EntityRecognitionSkillLanguageConverter() {
    }
}
