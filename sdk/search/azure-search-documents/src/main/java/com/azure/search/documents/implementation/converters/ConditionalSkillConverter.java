// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ConditionalSkill;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ConditionalSkill} and
 * {@link ConditionalSkill}.
 */
public final class ConditionalSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ConditionalSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ConditionalSkill} to {@link ConditionalSkill}.
     */
    public static ConditionalSkill map(com.azure.search.documents.implementation.models.ConditionalSkill obj) {
        if (obj == null) {
            return null;
        }
        ConditionalSkill conditionalSkill = new ConditionalSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            conditionalSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            conditionalSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        conditionalSkill.setName(_name);

        String _context = obj.getContext();
        conditionalSkill.setContext(_context);

        String _description = obj.getDescription();
        conditionalSkill.setDescription(_description);
        return conditionalSkill;
    }

    /**
     * Maps from {@link ConditionalSkill} to {@link com.azure.search.documents.implementation.models.ConditionalSkill}.
     */
    public static com.azure.search.documents.implementation.models.ConditionalSkill map(ConditionalSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ConditionalSkill conditionalSkill =
            new com.azure.search.documents.implementation.models.ConditionalSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            conditionalSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            conditionalSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        conditionalSkill.setName(_name);

        String _context = obj.getContext();
        conditionalSkill.setContext(_context);

        String _description = obj.getDescription();
        conditionalSkill.setDescription(_description);
        return conditionalSkill;
    }
}
