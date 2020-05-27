// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ConditionalSkill;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ConditionalSkill} and
 * {@link ConditionalSkill}.
 */
public final class ConditionalSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ConditionalSkill} to {@link ConditionalSkill}.
     */
    public static ConditionalSkill map(com.azure.search.documents.indexes.implementation.models.ConditionalSkill obj) {
        if (obj == null) {
            return null;
        }
        ConditionalSkill conditionalSkill = new ConditionalSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            conditionalSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            conditionalSkill.setInputs(inputs);
        }

        String name = obj.getName();
        conditionalSkill.setName(name);

        String context = obj.getContext();
        conditionalSkill.setContext(context);

        String description = obj.getDescription();
        conditionalSkill.setDescription(description);
        return conditionalSkill;
    }

    /**
     * Maps from {@link ConditionalSkill} to {@link com.azure.search.documents.indexes.implementation.models.ConditionalSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ConditionalSkill map(ConditionalSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.ConditionalSkill conditionalSkill =
            new com.azure.search.documents.indexes.implementation.models.ConditionalSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            conditionalSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            conditionalSkill.setInputs(inputs);
        }

        String name = obj.getName();
        conditionalSkill.setName(name);

        String context = obj.getContext();
        conditionalSkill.setContext(context);

        String description = obj.getDescription();
        conditionalSkill.setDescription(description);
        return conditionalSkill;
    }

    private ConditionalSkillConverter() {
    }
}
