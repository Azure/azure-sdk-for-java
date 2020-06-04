// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.TextTranslationSkill;
import com.azure.search.documents.indexes.models.TextTranslationSkillLanguage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.TextTranslationSkill} and
 * {@link TextTranslationSkill}.
 */
public final class TextTranslationSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.TextTranslationSkill} to
     * {@link TextTranslationSkill}.
     */
    public static TextTranslationSkill map(com.azure.search.documents.indexes.implementation.models.TextTranslationSkill obj) {
        if (obj == null) {
            return null;
        }
        TextTranslationSkill textTranslationSkill = new TextTranslationSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            textTranslationSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            textTranslationSkill.setInputs(inputs);
        }

        String name = obj.getName();
        textTranslationSkill.setName(name);

        String context = obj.getContext();
        textTranslationSkill.setContext(context);

        String description = obj.getDescription();
        textTranslationSkill.setDescription(description);

        if (obj.getDefaultToLanguageCode() != null) {
            TextTranslationSkillLanguage defaultToLanguageCode =
                TextTranslationSkillLanguageConverter.map(obj.getDefaultToLanguageCode());
            textTranslationSkill.setDefaultToLanguageCode(defaultToLanguageCode);
        }

        if (obj.getDefaultFromLanguageCode() != null) {
            TextTranslationSkillLanguage defaultFromLanguageCode =
                TextTranslationSkillLanguageConverter.map(obj.getDefaultFromLanguageCode());
            textTranslationSkill.setDefaultFromLanguageCode(defaultFromLanguageCode);
        }

        if (obj.getSuggestedFrom() != null) {
            TextTranslationSkillLanguage suggestedFrom =
                TextTranslationSkillLanguageConverter.map(obj.getSuggestedFrom());
            textTranslationSkill.setSuggestedFrom(suggestedFrom);
        }
        return textTranslationSkill;
    }

    /**
     * Maps from {@link TextTranslationSkill} to
     * {@link com.azure.search.documents.indexes.implementation.models.TextTranslationSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.TextTranslationSkill map(TextTranslationSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.TextTranslationSkill textTranslationSkill =
            new com.azure.search.documents.indexes.implementation.models.TextTranslationSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            textTranslationSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            textTranslationSkill.setInputs(inputs);
        }

        String name = obj.getName();
        textTranslationSkill.setName(name);

        String context = obj.getContext();
        textTranslationSkill.setContext(context);

        String description = obj.getDescription();
        textTranslationSkill.setDescription(description);

        if (obj.getDefaultToLanguageCode() != null) {
            com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage defaultToLanguageCode =
                TextTranslationSkillLanguageConverter.map(obj.getDefaultToLanguageCode());
            textTranslationSkill.setDefaultToLanguageCode(defaultToLanguageCode);
        }

        if (obj.getDefaultFromLanguageCode() != null) {
            com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage defaultFromLanguageCode =
                TextTranslationSkillLanguageConverter.map(obj.getDefaultFromLanguageCode());
            textTranslationSkill.setDefaultFromLanguageCode(defaultFromLanguageCode);
        }

        if (obj.getSuggestedFrom() != null) {
            com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage suggestedFrom =
                TextTranslationSkillLanguageConverter.map(obj.getSuggestedFrom());
            textTranslationSkill.setSuggestedFrom(suggestedFrom);
        }
        return textTranslationSkill;
    }

    private TextTranslationSkillConverter() {
    }
}
