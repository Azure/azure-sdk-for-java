// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.SplitSkill;
import com.azure.search.documents.indexes.models.SplitSkillLanguage;
import com.azure.search.documents.indexes.models.TextSplitMode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.SplitSkill} and {@link SplitSkill}.
 */
public final class SplitSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.SplitSkill} to {@link SplitSkill}.
     */
    public static SplitSkill map(com.azure.search.documents.indexes.implementation.models.SplitSkill obj) {
        if (obj == null) {
            return null;
        }

        List<OutputFieldMappingEntry> outputs = obj.getOutputs() == null ? null
            : obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
        List<InputFieldMappingEntry> inputs = obj.getInputs() == null ? null
            : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
        SplitSkill splitSkill = new SplitSkill(inputs, outputs);

        String name = obj.getName();
        splitSkill.setName(name);

        String context = obj.getContext();
        splitSkill.setContext(context);

        String description = obj.getDescription();
        splitSkill.setDescription(description);

        Integer maximumPageLength = obj.getMaximumPageLength();
        splitSkill.setMaximumPageLength(maximumPageLength);

        if (obj.getTextSplitMode() != null) {
            TextSplitMode textSplitMode = TextSplitModeConverter.map(obj.getTextSplitMode());
            splitSkill.setTextSplitMode(textSplitMode);
        }

        if (obj.getDefaultLanguageCode() != null) {
            SplitSkillLanguage defaultLanguageCode = SplitSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            splitSkill.setDefaultLanguageCode(defaultLanguageCode);
        }
        return splitSkill;
    }

    /**
     * Maps from {@link SplitSkill} to {@link com.azure.search.documents.indexes.implementation.models.SplitSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.SplitSkill map(SplitSkill obj) {
        if (obj == null) {
            return null;
        }

        List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
            obj.getOutputs() == null ? null
            : obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());

        List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
            obj.getOutputs() == null ? null
            : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());

        com.azure.search.documents.indexes.implementation.models.SplitSkill splitSkill =
            new com.azure.search.documents.indexes.implementation.models.SplitSkill(outputs, inputs);

        String name = obj.getName();
        splitSkill.setName(name);

        String context = obj.getContext();
        splitSkill.setContext(context);

        String description = obj.getDescription();
        splitSkill.setDescription(description);

        Integer maximumPageLength = obj.getMaximumPageLength();
        splitSkill.setMaximumPageLength(maximumPageLength);

        if (obj.getTextSplitMode() != null) {
            com.azure.search.documents.indexes.implementation.models.TextSplitMode textSplitMode =
                TextSplitModeConverter.map(obj.getTextSplitMode());
            splitSkill.setTextSplitMode(textSplitMode);
        }

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.indexes.implementation.models.SplitSkillLanguage defaultLanguageCode =
                SplitSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            splitSkill.setDefaultLanguageCode(defaultLanguageCode);
        }
        splitSkill.validate();
        return splitSkill;
    }

    private SplitSkillConverter() {
    }
}
