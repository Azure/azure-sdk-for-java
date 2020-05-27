// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SplitSkillLanguage;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SplitSkillLanguage} and
 * {@link SplitSkillLanguage}.
 */
public final class SplitSkillLanguageConverter {
    /**
     * Maps from enum {@link com.azure.search.documents.indexes.implementation.models.SplitSkillLanguage} to enum
     * {@link SplitSkillLanguage}.
     */
    public static SplitSkillLanguage map(com.azure.search.documents.indexes.implementation.models.SplitSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return SplitSkillLanguage.fromString(obj.toString());
    }

    /**
     * Maps from enum {@link SplitSkillLanguage} to enum
     * {@link com.azure.search.documents.indexes.implementation.models.SplitSkillLanguage}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SplitSkillLanguage map(SplitSkillLanguage obj) {
        if (obj == null) {
            return null;
        }
        return com.azure.search.documents.indexes.implementation.models.SplitSkillLanguage.fromString(obj.toString());
    }

    private SplitSkillLanguageConverter() {
    }
}
