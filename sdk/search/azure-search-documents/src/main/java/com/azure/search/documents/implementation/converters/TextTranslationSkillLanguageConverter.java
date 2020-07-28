// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.TextTranslationSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage} and
 * {@link TextTranslationSkillLanguage}.
 */
public final class TextTranslationSkillLanguageConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage} to enum
     * {@link TextTranslationSkillLanguage}.
     */
    public static TextTranslationSkillLanguage map(com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return TextTranslationSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link TextTranslationSkillLanguage} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage}.
     */
    public static com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage map(TextTranslationSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage.fromString(obj.toString());
    }

    private TextTranslationSkillLanguageConverter() {
    }
}
