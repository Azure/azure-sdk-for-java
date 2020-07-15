// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;
import com.azure.search.documents.indexes.models.ShaperSkill;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ShaperSkill} and {@link ShaperSkill}.
 */
public final class ShaperSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ShaperSkill} to {@link ShaperSkill}.
     */
    public static ShaperSkill map(com.azure.search.documents.indexes.implementation.models.ShaperSkill obj) {
        if (obj == null) {
            return null;
        }

        List<OutputFieldMappingEntry> outputs = obj.getOutputs() == null ? null
            : obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());

        List<InputFieldMappingEntry> inputs = obj.getInputs() == null ? null
            : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());

        ShaperSkill shaperSkill = new ShaperSkill(inputs, outputs);

        String name = obj.getName();
        shaperSkill.setName(name);

        String context = obj.getContext();
        shaperSkill.setContext(context);

        String description = obj.getDescription();
        shaperSkill.setDescription(description);
        return shaperSkill;
    }

    /**
     * Maps from {@link ShaperSkill} to {@link com.azure.search.documents.indexes.implementation.models.ShaperSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ShaperSkill map(ShaperSkill obj) {
        if (obj == null) {
            return null;
        }
        List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
            obj.getOutputs() == null ? null
                : obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());

        List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
            obj.getOutputs() == null ? null
                : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
        com.azure.search.documents.indexes.implementation.models.ShaperSkill shaperSkill =
            new com.azure.search.documents.indexes.implementation.models.ShaperSkill(inputs, outputs);


        String name = obj.getName();
        shaperSkill.setName(name);

        String context = obj.getContext();
        shaperSkill.setContext(context);

        String description = obj.getDescription();
        shaperSkill.setDescription(description);
        shaperSkill.validate();
        return shaperSkill;
    }

    private ShaperSkillConverter() {
    }
}
