// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.KeyPhraseExtractionSkill;
import com.azure.search.documents.models.KeyPhraseExtractionSkillLanguage;
import com.azure.search.documents.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill} and
 * {@link KeyPhraseExtractionSkill}.
 */
public final class KeyPhraseExtractionSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(KeyPhraseExtractionSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill} to
     * {@link KeyPhraseExtractionSkill}.
     */
    public static KeyPhraseExtractionSkill map(com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill obj) {
        if (obj == null) {
            return null;
        }
        KeyPhraseExtractionSkill keyPhraseExtractionSkill = new KeyPhraseExtractionSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            keyPhraseExtractionSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            keyPhraseExtractionSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        keyPhraseExtractionSkill.setName(_name);

        String _context = obj.getContext();
        keyPhraseExtractionSkill.setContext(_context);

        String _description = obj.getDescription();
        keyPhraseExtractionSkill.setDescription(_description);

        Integer _maxKeyPhraseCount = obj.getMaxKeyPhraseCount();
        keyPhraseExtractionSkill.setMaxKeyPhraseCount(_maxKeyPhraseCount);

        if (obj.getDefaultLanguageCode() != null) {
            KeyPhraseExtractionSkillLanguage _defaultLanguageCode =
                KeyPhraseExtractionSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            keyPhraseExtractionSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }
        return keyPhraseExtractionSkill;
    }

    /**
     * Maps from {@link KeyPhraseExtractionSkill} to
     * {@link com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill}.
     */
    public static com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill map(KeyPhraseExtractionSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill keyPhraseExtractionSkill =
            new com.azure.search.documents.implementation.models.KeyPhraseExtractionSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            keyPhraseExtractionSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            keyPhraseExtractionSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        keyPhraseExtractionSkill.setName(_name);

        String _context = obj.getContext();
        keyPhraseExtractionSkill.setContext(_context);

        String _description = obj.getDescription();
        keyPhraseExtractionSkill.setDescription(_description);

        Integer _maxKeyPhraseCount = obj.getMaxKeyPhraseCount();
        keyPhraseExtractionSkill.setMaxKeyPhraseCount(_maxKeyPhraseCount);

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.implementation.models.KeyPhraseExtractionSkillLanguage _defaultLanguageCode =
                KeyPhraseExtractionSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            keyPhraseExtractionSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }
        return keyPhraseExtractionSkill;
    }
}
