// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SentimentSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SentimentSkillLanguage} and
 * {@link SentimentSkillLanguage}.
 */
public final class SentimentSkillLanguageConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.SentimentSkillLanguage} to enum
     * {@link SentimentSkillLanguage}.
     */
    public static SentimentSkillLanguage map(com.azure.search.documents.indexes.implementation.models.SentimentSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return SentimentSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link SentimentSkillLanguage} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.SentimentSkillLanguage}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SentimentSkillLanguage map(SentimentSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.SentimentSkillLanguage.fromString(obj.toString());
    }

    private SentimentSkillLanguageConverter() {
    }
}
