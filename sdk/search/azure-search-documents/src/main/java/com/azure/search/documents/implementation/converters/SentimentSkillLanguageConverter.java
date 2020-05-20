// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.SentimentSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SentimentSkillLanguage} and
 * {@link SentimentSkillLanguage}.
 */
public final class SentimentSkillLanguageConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SentimentSkillLanguageConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.SentimentSkillLanguage} to enum
     * {@link SentimentSkillLanguage}.
     */
    public static SentimentSkillLanguage map(com.azure.search.documents.implementation.models.SentimentSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return SentimentSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link SentimentSkillLanguage} to enum
     * {@link com.azure.search.documents.implementation.models.SentimentSkillLanguage}.
     */
    public static com.azure.search.documents.implementation.models.SentimentSkillLanguage map(SentimentSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.implementation.models.SentimentSkillLanguage.fromString(obj.toString());
    }
}
