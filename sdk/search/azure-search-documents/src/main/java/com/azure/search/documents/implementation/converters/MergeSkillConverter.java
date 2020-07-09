// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.MergeSkill;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.MergeSkill} and {@link MergeSkill}.
 */
public final class MergeSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.MergeSkill} to {@link MergeSkill}.
     */
    public static MergeSkill map(com.azure.search.documents.indexes.implementation.models.MergeSkill obj) {
        if (obj == null) {
            return null;
        }

        List<OutputFieldMappingEntry> outputs = obj.getOutputs() == null ? null
            : obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
        List<InputFieldMappingEntry> inputs = obj.getInputs() == null ? null
            : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
        MergeSkill mergeSkill = new MergeSkill(inputs, outputs);

        String name = obj.getName();
        mergeSkill.setName(name);

        String context = obj.getContext();
        mergeSkill.setContext(context);

        String description = obj.getDescription();
        mergeSkill.setDescription(description);

        String insertPostTag = obj.getInsertPostTag();
        mergeSkill.setInsertPostTag(insertPostTag);

        String insertPreTag = obj.getInsertPreTag();
        mergeSkill.setInsertPreTag(insertPreTag);
        return mergeSkill;
    }

    /**
     * Maps from {@link MergeSkill} to {@link com.azure.search.documents.indexes.implementation.models.MergeSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.MergeSkill map(MergeSkill obj) {
        if (obj == null) {
            return null;
        }

        List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
            obj.getOutputs() == null ? null
                : obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());

        List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
            obj.getInputs() == null ? null
                : obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
        com.azure.search.documents.indexes.implementation.models.MergeSkill mergeSkill =
            new com.azure.search.documents.indexes.implementation.models.MergeSkill(inputs, outputs);

        String name = obj.getName();
        mergeSkill.setName(name);

        String context = obj.getContext();
        mergeSkill.setContext(context);

        String description = obj.getDescription();
        mergeSkill.setDescription(description);

        String insertPostTag = obj.getInsertPostTag();
        mergeSkill.setInsertPostTag(insertPostTag);

        String insertPreTag = obj.getInsertPreTag();
        mergeSkill.setInsertPreTag(insertPreTag);
        mergeSkill.validate();
        return mergeSkill;
    }

    private MergeSkillConverter() {
    }
}
