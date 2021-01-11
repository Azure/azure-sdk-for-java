// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.LanguageDetectionSkill;

import java.util.List;
import java.util.stream.Collectors;

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

        List<InputFieldMappingEntry> inputs = obj.getInputs() == null ? null :
            obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
        LanguageDetectionSkill languageDetectionSkill = new LanguageDetectionSkill(inputs, obj.getOutputs());

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

        List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
            obj.getOutputs() == null ? null :
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
        com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill languageDetectionSkill =
            new com.azure.search.documents.indexes.implementation.models.LanguageDetectionSkill(inputs,
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
