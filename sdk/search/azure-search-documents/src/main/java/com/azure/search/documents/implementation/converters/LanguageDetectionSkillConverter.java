// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.LanguageDetectionSkill;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill} and
 * {@link LanguageDetectionSkill}.
 */
public final class LanguageDetectionSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill} to
     * {@link LanguageDetectionSkill}.
     */
    public static LanguageDetectionSkill map(com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill obj) {
        if (obj == null) {
            return null;
        }

        LanguageDetectionSkill languageDetectionSkill = new LanguageDetectionSkill(obj.getInputs(), obj.getOutputs());

        String name = obj.getName();
        languageDetectionSkill.setName(name);

        String context = obj.getContext();
        languageDetectionSkill.setContext(context);

        String description = obj.getDescription();
        languageDetectionSkill.setDescription(description);
        return languageDetectionSkill;
    }

    /**
     * Maps from {@link LanguageDetectionSkill} to
     * {@link com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill map(LanguageDetectionSkill obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill languageDetectionSkill =
            new com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill(obj.getInputs(),
                obj.getOutputs());

        String name = obj.getName();
        languageDetectionSkill.setName(name);

        String context = obj.getContext();
        languageDetectionSkill.setContext(context);

        String description = obj.getDescription();
        languageDetectionSkill.setDescription(description);

        return languageDetectionSkill;
    }

    private LanguageDetectionSkillConverter() {
    }
}
