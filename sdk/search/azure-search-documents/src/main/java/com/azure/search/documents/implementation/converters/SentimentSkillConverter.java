// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.SentimentSkill;
import com.azure.search.documents.models.SentimentSkillLanguage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SentimentSkill} and
 * {@link SentimentSkill}.
 */
public final class SentimentSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SentimentSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SentimentSkill} to {@link SentimentSkill}.
     */
    public static SentimentSkill map(com.azure.search.documents.implementation.models.SentimentSkill obj) {
        if (obj == null) {
            return null;
        }
        SentimentSkill sentimentSkill = new SentimentSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            sentimentSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            sentimentSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        sentimentSkill.setName(_name);

        String _context = obj.getContext();
        sentimentSkill.setContext(_context);

        String _description = obj.getDescription();
        sentimentSkill.setDescription(_description);

        if (obj.getDefaultLanguageCode() != null) {
            SentimentSkillLanguage _defaultLanguageCode =
                SentimentSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            sentimentSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }
        return sentimentSkill;
    }

    /**
     * Maps from {@link SentimentSkill} to {@link com.azure.search.documents.implementation.models.SentimentSkill}.
     */
    public static com.azure.search.documents.implementation.models.SentimentSkill map(SentimentSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SentimentSkill sentimentSkill =
            new com.azure.search.documents.implementation.models.SentimentSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            sentimentSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            sentimentSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        sentimentSkill.setName(_name);

        String _context = obj.getContext();
        sentimentSkill.setContext(_context);

        String _description = obj.getDescription();
        sentimentSkill.setDescription(_description);

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.implementation.models.SentimentSkillLanguage _defaultLanguageCode =
                SentimentSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            sentimentSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }
        return sentimentSkill;
    }
}
