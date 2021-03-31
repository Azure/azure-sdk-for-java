// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.SentimentSkill;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SentimentSkill} and
 * {@link SentimentSkill}.
 */
public final class SentimentSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SentimentSkill} to {@link SentimentSkill}.
     */
    public static SentimentSkill map(com.azure.search.documents.indexes.implementation.models.SentimentSkill obj) {
        if (obj == null) {
            return null;
        }
        SentimentSkill sentimentSkill = new SentimentSkill(obj.getInputs(), obj.getOutputs());

        String name = obj.getName();
        sentimentSkill.setName(name);

        String context = obj.getContext();
        sentimentSkill.setContext(context);

        String description = obj.getDescription();
        sentimentSkill.setDescription(description);

        if (obj.getDefaultLanguageCode() != null) {
            sentimentSkill.setDefaultLanguageCode(obj.getDefaultLanguageCode());
        }
        return sentimentSkill;
    }

    /**
     * Maps from {@link SentimentSkill} to {@link com.azure.search.documents.indexes.implementation.models.SentimentSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SentimentSkill map(SentimentSkill obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.SentimentSkill sentimentSkill =
            new com.azure.search.documents.indexes.implementation.models.SentimentSkill(obj.getInputs(),
                obj.getOutputs());


        String name = obj.getName();
        sentimentSkill.setName(name);

        String context = obj.getContext();
        sentimentSkill.setContext(context);

        String description = obj.getDescription();
        sentimentSkill.setDescription(description);

        if (obj.getDefaultLanguageCode() != null) {
            sentimentSkill.setDefaultLanguageCode(obj.getDefaultLanguageCode());
        }

        return sentimentSkill;
    }

    private SentimentSkillConverter() {
    }
}
