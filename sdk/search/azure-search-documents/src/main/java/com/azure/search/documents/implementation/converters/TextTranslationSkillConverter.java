// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.TextTranslationSkill;
import com.azure.search.documents.models.TextTranslationSkillLanguage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.TextTranslationSkill} and
 * {@link TextTranslationSkill}.
 */
public final class TextTranslationSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TextTranslationSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.TextTranslationSkill} to
     * {@link TextTranslationSkill}.
     */
    public static TextTranslationSkill map(com.azure.search.documents.implementation.models.TextTranslationSkill obj) {
        if (obj == null) {
            return null;
        }
        TextTranslationSkill textTranslationSkill = new TextTranslationSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            textTranslationSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            textTranslationSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        textTranslationSkill.setName(_name);

        String _context = obj.getContext();
        textTranslationSkill.setContext(_context);

        String _description = obj.getDescription();
        textTranslationSkill.setDescription(_description);

        if (obj.getDefaultToLanguageCode() != null) {
            TextTranslationSkillLanguage _defaultToLanguageCode =
                TextTranslationSkillLanguageConverter.map(obj.getDefaultToLanguageCode());
            textTranslationSkill.setDefaultToLanguageCode(_defaultToLanguageCode);
        }

        if (obj.getDefaultFromLanguageCode() != null) {
            TextTranslationSkillLanguage _defaultFromLanguageCode =
                TextTranslationSkillLanguageConverter.map(obj.getDefaultFromLanguageCode());
            textTranslationSkill.setDefaultFromLanguageCode(_defaultFromLanguageCode);
        }

        if (obj.getSuggestedFrom() != null) {
            TextTranslationSkillLanguage _suggestedFrom =
                TextTranslationSkillLanguageConverter.map(obj.getSuggestedFrom());
            textTranslationSkill.setSuggestedFrom(_suggestedFrom);
        }
        return textTranslationSkill;
    }

    /**
     * Maps from {@link TextTranslationSkill} to
     * {@link com.azure.search.documents.implementation.models.TextTranslationSkill}.
     */
    public static com.azure.search.documents.implementation.models.TextTranslationSkill map(TextTranslationSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.TextTranslationSkill textTranslationSkill =
            new com.azure.search.documents.implementation.models.TextTranslationSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            textTranslationSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            textTranslationSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        textTranslationSkill.setName(_name);

        String _context = obj.getContext();
        textTranslationSkill.setContext(_context);

        String _description = obj.getDescription();
        textTranslationSkill.setDescription(_description);

        if (obj.getDefaultToLanguageCode() != null) {
            com.azure.search.documents.implementation.models.TextTranslationSkillLanguage _defaultToLanguageCode =
                TextTranslationSkillLanguageConverter.map(obj.getDefaultToLanguageCode());
            textTranslationSkill.setDefaultToLanguageCode(_defaultToLanguageCode);
        }

        if (obj.getDefaultFromLanguageCode() != null) {
            com.azure.search.documents.implementation.models.TextTranslationSkillLanguage _defaultFromLanguageCode =
                TextTranslationSkillLanguageConverter.map(obj.getDefaultFromLanguageCode());
            textTranslationSkill.setDefaultFromLanguageCode(_defaultFromLanguageCode);
        }

        if (obj.getSuggestedFrom() != null) {
            com.azure.search.documents.implementation.models.TextTranslationSkillLanguage _suggestedFrom =
                TextTranslationSkillLanguageConverter.map(obj.getSuggestedFrom());
            textTranslationSkill.setSuggestedFrom(_suggestedFrom);
        }
        return textTranslationSkill;
    }
}
