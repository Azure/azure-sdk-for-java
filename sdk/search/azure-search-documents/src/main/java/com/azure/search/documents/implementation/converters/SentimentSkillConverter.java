// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SentimentSkill;

import java.util.List;
import java.util.stream.Collectors;

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

        List<InputFieldMappingEntry> inputs = obj.getInputs() == null ? null
            : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());

        SentimentSkill sentimentSkill = new SentimentSkill(inputs, obj.getOutputs());

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

        List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
            obj.getOutputs() == null ? null
                : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());

        com.azure.search.documents.indexes.implementation.models.SentimentSkill sentimentSkill =
            new com.azure.search.documents.indexes.implementation.models.SentimentSkill(inputs, obj.getOutputs());


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
