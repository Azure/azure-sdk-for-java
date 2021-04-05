// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ShaperSkill;

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

        ShaperSkill shaperSkill = new ShaperSkill(obj.getInputs(), obj.getOutputs());

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

        com.azure.search.documents.indexes.implementation.models.ShaperSkill shaperSkill =
            new com.azure.search.documents.indexes.implementation.models.ShaperSkill(obj.getInputs(), obj.getOutputs());

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
