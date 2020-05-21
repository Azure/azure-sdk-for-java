// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OutputFieldMappingEntry;
import com.azure.search.documents.models.ShaperSkill;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ShaperSkill} and {@link ShaperSkill}.
 */
public final class ShaperSkillConverter {


    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ShaperSkill} to {@link ShaperSkill}.
     */
    public static ShaperSkill map(com.azure.search.documents.implementation.models.ShaperSkill obj) {
        if (obj == null) {
            return null;
        }
        ShaperSkill shaperSkill = new ShaperSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            shaperSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            shaperSkill.setInputs(inputs);
        }

        String name = obj.getName();
        shaperSkill.setName(name);

        String context = obj.getContext();
        shaperSkill.setContext(context);

        String description = obj.getDescription();
        shaperSkill.setDescription(description);
        return shaperSkill;
    }

    /**
     * Maps from {@link ShaperSkill} to {@link com.azure.search.documents.implementation.models.ShaperSkill}.
     */
    public static com.azure.search.documents.implementation.models.ShaperSkill map(ShaperSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ShaperSkill shaperSkill =
            new com.azure.search.documents.implementation.models.ShaperSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            shaperSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            shaperSkill.setInputs(inputs);
        }

        String name = obj.getName();
        shaperSkill.setName(name);

        String context = obj.getContext();
        shaperSkill.setContext(context);

        String description = obj.getDescription();
        shaperSkill.setDescription(description);
        return shaperSkill;
    }

    private ShaperSkillConverter() {
    }
}
