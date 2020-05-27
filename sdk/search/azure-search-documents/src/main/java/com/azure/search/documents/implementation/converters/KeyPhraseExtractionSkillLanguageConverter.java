// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.KeyPhraseExtractionSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkillLanguage} and
 * {@link KeyPhraseExtractionSkillLanguage}.
 */
public final class KeyPhraseExtractionSkillLanguageConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkillLanguage} to enum
     * {@link KeyPhraseExtractionSkillLanguage}.
     */
    public static KeyPhraseExtractionSkillLanguage map(com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return KeyPhraseExtractionSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link KeyPhraseExtractionSkillLanguage} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkillLanguage}.
     */
    public static com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkillLanguage map(KeyPhraseExtractionSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkillLanguage.fromString(obj.toString());
    }

    private KeyPhraseExtractionSkillLanguageConverter() {
    }
}
