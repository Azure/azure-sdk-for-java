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
    private static final ClientLogger LOGGER = new ClientLogger(LanguageDetectionSkillConverter.class);

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
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            languageDetectionSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            languageDetectionSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        languageDetectionSkill.setName(_name);

        String _context = obj.getContext();
        languageDetectionSkill.setContext(_context);

        String _description = obj.getDescription();
        languageDetectionSkill.setDescription(_description);
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
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            languageDetectionSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            languageDetectionSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        languageDetectionSkill.setName(_name);

        String _context = obj.getContext();
        languageDetectionSkill.setContext(_context);

        String _description = obj.getDescription();
        languageDetectionSkill.setDescription(_description);
        return languageDetectionSkill;
    }
}
