// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.TextTranslationSkill;

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

        List<InputFieldMappingEntry> inputs = obj.getInputs() == null ? null
            : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());

        TextTranslationSkill textTranslationSkill = new TextTranslationSkill(inputs, obj.getOutputs(),
            obj.getDefaultToLanguageCode());

        String name = obj.getName();
        textTranslationSkill.setName(name);

        String context = obj.getContext();
        textTranslationSkill.setContext(context);

        String description = obj.getDescription();
        textTranslationSkill.setDescription(description);

        if (obj.getDefaultFromLanguageCode() != null) {
            textTranslationSkill.setDefaultFromLanguageCode(obj.getDefaultFromLanguageCode());
        }

        if (obj.getSuggestedFrom() != null) {
            textTranslationSkill.setSuggestedFrom(obj.getSuggestedFrom());
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

        List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
            obj.getOutputs() == null ? null
                : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());

        com.azure.search.documents.indexes.implementation.models.TextTranslationSkill textTranslationSkill =
            new com.azure.search.documents.indexes.implementation.models.TextTranslationSkill(inputs, obj.getOutputs(),
                obj.getDefaultToLanguageCode());

        String name = obj.getName();
        textTranslationSkill.setName(name);

        String context = obj.getContext();
        textTranslationSkill.setContext(context);

        String description = obj.getDescription();
        textTranslationSkill.setDescription(description);

        if (obj.getDefaultFromLanguageCode() != null) {
            textTranslationSkill.setDefaultFromLanguageCode(obj.getDefaultFromLanguageCode());
        }

        if (obj.getSuggestedFrom() != null) {
            textTranslationSkill.setSuggestedFrom(obj.getSuggestedFrom());
        }

        return textTranslationSkill;
    }

    private TextTranslationSkillConverter() {
    }
}
