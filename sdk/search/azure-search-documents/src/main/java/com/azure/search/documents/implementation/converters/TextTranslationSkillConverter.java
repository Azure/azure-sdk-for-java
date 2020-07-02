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

        List<OutputFieldMappingEntry> outputs = obj.getOutputs() == null ? null
            : obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());

        List<InputFieldMappingEntry> inputs = obj.getInputs() == null ? null
            : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());

        TextTranslationSkillLanguage defaultToLanguageCode = obj.getDefaultToLanguageCode() == null ? null
            : TextTranslationSkillLanguageConverter.map(obj.getDefaultToLanguageCode());

        TextTranslationSkill textTranslationSkill = new TextTranslationSkill(inputs, outputs, defaultToLanguageCode);

        String name = obj.getName();
        textTranslationSkill.setName(name);

        String context = obj.getContext();
        textTranslationSkill.setContext(context);

        String description = obj.getDescription();
        textTranslationSkill.setDescription(description);

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
        List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
            obj.getOutputs() == null ? null
                : obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());

        List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
            obj.getOutputs() == null ? null
                : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());

        com.azure.search.documents.indexes.implementation.models.TextTranslationSkillLanguage defaultToLanguageCode =
            obj.getDefaultToLanguageCode() == null ? null
                : TextTranslationSkillLanguageConverter.map(obj.getDefaultToLanguageCode());
        com.azure.search.documents.indexes.implementation.models.TextTranslationSkill textTranslationSkill =
            new com.azure.search.documents.indexes.implementation.models.TextTranslationSkill(inputs, outputs,
                defaultToLanguageCode);

        String name = obj.getName();
        textTranslationSkill.setName(name);

        String context = obj.getContext();
        textTranslationSkill.setContext(context);

        String description = obj.getDescription();
        textTranslationSkill.setDescription(description);

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
        textTranslationSkill.validate();
        return textTranslationSkill;
    }

    private TextTranslationSkillConverter() {
    }
}
