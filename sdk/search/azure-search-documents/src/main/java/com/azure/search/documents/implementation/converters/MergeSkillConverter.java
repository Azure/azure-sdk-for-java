// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.MergeSkill;
import com.azure.search.documents.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.MergeSkill} and {@link MergeSkill}.
 */
public final class MergeSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(MergeSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.MergeSkill} to {@link MergeSkill}.
     */
    public static MergeSkill map(com.azure.search.documents.implementation.models.MergeSkill obj) {
        if (obj == null) {
            return null;
        }
        MergeSkill mergeSkill = new MergeSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            mergeSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            mergeSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        mergeSkill.setName(_name);

        String _context = obj.getContext();
        mergeSkill.setContext(_context);

        String _description = obj.getDescription();
        mergeSkill.setDescription(_description);

        String _insertPostTag = obj.getInsertPostTag();
        mergeSkill.setInsertPostTag(_insertPostTag);

        String _insertPreTag = obj.getInsertPreTag();
        mergeSkill.setInsertPreTag(_insertPreTag);
        return mergeSkill;
    }

    /**
     * Maps from {@link MergeSkill} to {@link com.azure.search.documents.implementation.models.MergeSkill}.
     */
    public static com.azure.search.documents.implementation.models.MergeSkill map(MergeSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.MergeSkill mergeSkill =
            new com.azure.search.documents.implementation.models.MergeSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            mergeSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            mergeSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        mergeSkill.setName(_name);

        String _context = obj.getContext();
        mergeSkill.setContext(_context);

        String _description = obj.getDescription();
        mergeSkill.setDescription(_description);

        String _insertPostTag = obj.getInsertPostTag();
        mergeSkill.setInsertPostTag(_insertPostTag);

        String _insertPreTag = obj.getInsertPreTag();
        mergeSkill.setInsertPreTag(_insertPreTag);
        return mergeSkill;
    }
}
