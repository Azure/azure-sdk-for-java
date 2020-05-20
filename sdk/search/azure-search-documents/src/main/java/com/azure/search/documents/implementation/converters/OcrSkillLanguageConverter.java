// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.OcrSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.OcrSkillLanguage} and
 * {@link OcrSkillLanguage}.
 */
public final class OcrSkillLanguageConverter {
    private static final ClientLogger LOGGER = new ClientLogger(OcrSkillLanguageConverter.class);

    /**
     * Maps from enum {@link com.azure.search.documents.implementation.models.OcrSkillLanguage} to enum
     * {@link OcrSkillLanguage}.
     */
    public static OcrSkillLanguage map(com.azure.search.documents.implementation.models.OcrSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return OcrSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link OcrSkillLanguage} to enum
     * {@link com.azure.search.documents.implementation.models.OcrSkillLanguage}.
     */
    public static com.azure.search.documents.implementation.models.OcrSkillLanguage map(OcrSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.implementation.models.OcrSkillLanguage.fromString(obj.toString());
    }
}
