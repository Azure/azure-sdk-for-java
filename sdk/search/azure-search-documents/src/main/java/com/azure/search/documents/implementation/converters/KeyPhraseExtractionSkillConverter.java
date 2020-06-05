// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.KeyPhraseExtractionSkill;
import com.azure.search.documents.indexes.models.KeyPhraseExtractionSkillLanguage;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkill} and
 * {@link KeyPhraseExtractionSkill}.
 */
public final class KeyPhraseExtractionSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkill} to
     * {@link KeyPhraseExtractionSkill}.
     */
    public static KeyPhraseExtractionSkill map(com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkill obj) {
        if (obj == null) {
            return null;
        }
        KeyPhraseExtractionSkill keyPhraseExtractionSkill = new KeyPhraseExtractionSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            keyPhraseExtractionSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            keyPhraseExtractionSkill.setInputs(inputs);
        }

        String name = obj.getName();
        keyPhraseExtractionSkill.setName(name);

        String context = obj.getContext();
        keyPhraseExtractionSkill.setContext(context);

        String description = obj.getDescription();
        keyPhraseExtractionSkill.setDescription(description);

        Integer maxKeyPhraseCount = obj.getMaxKeyPhraseCount();
        keyPhraseExtractionSkill.setMaxKeyPhraseCount(maxKeyPhraseCount);

        if (obj.getDefaultLanguageCode() != null) {
            KeyPhraseExtractionSkillLanguage defaultLanguageCode =
                KeyPhraseExtractionSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            keyPhraseExtractionSkill.setDefaultLanguageCode(defaultLanguageCode);
        }
        return keyPhraseExtractionSkill;
    }

    /**
     * Maps from {@link KeyPhraseExtractionSkill} to
     * {@link com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkill map(KeyPhraseExtractionSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkill keyPhraseExtractionSkill =
            new com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            keyPhraseExtractionSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            keyPhraseExtractionSkill.setInputs(inputs);
        }

        String name = obj.getName();
        keyPhraseExtractionSkill.setName(name);

        String context = obj.getContext();
        keyPhraseExtractionSkill.setContext(context);

        String description = obj.getDescription();
        keyPhraseExtractionSkill.setDescription(description);

        Integer maxKeyPhraseCount = obj.getMaxKeyPhraseCount();
        keyPhraseExtractionSkill.setMaxKeyPhraseCount(maxKeyPhraseCount);

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.indexes.implementation.models.KeyPhraseExtractionSkillLanguage defaultLanguageCode =
                KeyPhraseExtractionSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            keyPhraseExtractionSkill.setDefaultLanguageCode(defaultLanguageCode);
        }
        return keyPhraseExtractionSkill;
    }

    private KeyPhraseExtractionSkillConverter() {
    }
}
