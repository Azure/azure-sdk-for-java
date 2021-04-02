// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.CustomEntityLookupSkill;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.CustomEntityLookupSkill} and
 * {@link CustomEntityLookupSkill}.
 */
public final class CustomEntityLookupSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.CustomEntityLookupSkill} to {@link
     * CustomEntityLookupSkill}.
     */
    public static CustomEntityLookupSkill map(
        com.azure.search.documents.indexes.implementation.models.CustomEntityLookupSkill obj) {
        if (obj == null) {
            return null;
        }

        CustomEntityLookupSkill customEntityLookupSkill = new CustomEntityLookupSkill(obj.getInputs(),
            obj.getOutputs());

        customEntityLookupSkill.setDefaultLanguageCode(obj.getDefaultLanguageCode());
        customEntityLookupSkill.setEntitiesDefinitionUri(obj.getEntitiesDefinitionUri());
        customEntityLookupSkill.setInlineEntitiesDefinition(obj.getInlineEntitiesDefinition());
        customEntityLookupSkill.setGlobalDefaultCaseSensitive(obj.isGlobalDefaultCaseSensitive());
        customEntityLookupSkill.setGlobalDefaultAccentSensitive(obj.isGlobalDefaultAccentSensitive());
        customEntityLookupSkill.setGlobalDefaultFuzzyEditDistance(obj.getGlobalDefaultFuzzyEditDistance());

        return customEntityLookupSkill;
    }

    /**
     * Maps from {@link CustomEntityLookupSkill} to
     * {@link com.azure.search.documents.indexes.implementation.models.CustomEntityLookupSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.CustomEntityLookupSkill map(
        CustomEntityLookupSkill obj) {
        if (obj == null) {
            return null;
        }

        com.azure.search.documents.indexes.implementation.models.CustomEntityLookupSkill customEntityLookupSkill =
            new com.azure.search.documents.indexes.implementation.models.CustomEntityLookupSkill(obj.getInputs(),
                obj.getOutputs());

        customEntityLookupSkill.setDefaultLanguageCode(obj.getDefaultLanguageCode());
        customEntityLookupSkill.setEntitiesDefinitionUri(obj.getEntitiesDefinitionUri());
        customEntityLookupSkill.setInlineEntitiesDefinition(obj.getInlineEntitiesDefinition());
        customEntityLookupSkill.setGlobalDefaultCaseSensitive(obj.isGlobalDefaultCaseSensitive());
        customEntityLookupSkill.setGlobalDefaultAccentSensitive(obj.isGlobalDefaultAccentSensitive());
        customEntityLookupSkill.setGlobalDefaultFuzzyEditDistance(obj.getGlobalDefaultFuzzyEditDistance());

        return customEntityLookupSkill;
    }
}
