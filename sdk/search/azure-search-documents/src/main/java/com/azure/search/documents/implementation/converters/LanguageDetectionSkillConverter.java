// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.LanguageDetectionSkill;
import com.azure.search.documents.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.LanguageDetectionSkill} and
 * {@link LanguageDetectionSkill}.
 */
public final class LanguageDetectionSkillConverter {


    /**
     * Maps from {@link com.azure.search.documents.implementation.models.LanguageDetectionSkill} to
     * {@link LanguageDetectionSkill}.
     */
    public static LanguageDetectionSkill map(com.azure.search.documents.implementation.models.LanguageDetectionSkill obj) {
        if (obj == null) {
            return null;
        }
        LanguageDetectionSkill languageDetectionSkill = new LanguageDetectionSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            languageDetectionSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            languageDetectionSkill.setInputs(inputs);
        }

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
     * {@link com.azure.search.documents.implementation.models.LanguageDetectionSkill}.
     */
    public static com.azure.search.documents.implementation.models.LanguageDetectionSkill map(LanguageDetectionSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.LanguageDetectionSkill languageDetectionSkill =
            new com.azure.search.documents.implementation.models.LanguageDetectionSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            languageDetectionSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            languageDetectionSkill.setInputs(inputs);
        }

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
