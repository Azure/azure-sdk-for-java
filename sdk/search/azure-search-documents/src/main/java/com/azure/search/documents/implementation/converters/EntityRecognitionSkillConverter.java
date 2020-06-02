// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.EntityCategory;
import com.azure.search.documents.indexes.models.EntityRecognitionSkill;
import com.azure.search.documents.indexes.models.EntityRecognitionSkillLanguage;
import com.azure.search.documents.indexes.models.InputFieldMappingEntry;
import com.azure.search.documents.indexes.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkill} and
 * {@link EntityRecognitionSkill}.
 */
public final class EntityRecognitionSkillConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkill} to
     * {@link EntityRecognitionSkill}.
     */
    public static EntityRecognitionSkill map(com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkill obj) {
        if (obj == null) {
            return null;
        }
        EntityRecognitionSkill entityRecognitionSkill = new EntityRecognitionSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setInputs(inputs);
        }

        String name = obj.getName();
        entityRecognitionSkill.setName(name);

        String context = obj.getContext();
        entityRecognitionSkill.setContext(context);

        String description = obj.getDescription();
        entityRecognitionSkill.setDescription(description);

        Boolean includeTypelessEntities = obj.isIncludeTypelessEntities();
        entityRecognitionSkill.setTypelessEntitiesIncluded(includeTypelessEntities);

        if (obj.getDefaultLanguageCode() != null) {
            EntityRecognitionSkillLanguage defaultLanguageCode =
                EntityRecognitionSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            entityRecognitionSkill.setDefaultLanguageCode(defaultLanguageCode);
        }

        if (obj.getCategories() != null) {
            List<EntityCategory> categories =
                obj.getCategories().stream().map(EntityCategoryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setCategories(categories);
        }

        Double minimumPrecision = obj.getMinimumPrecision();
        entityRecognitionSkill.setMinimumPrecision(minimumPrecision);
        return entityRecognitionSkill;
    }

    /**
     * Maps from {@link EntityRecognitionSkill} to
     * {@link com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkill}.
     */
    public static com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkill map(EntityRecognitionSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkill entityRecognitionSkill =
            new com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.OutputFieldMappingEntry> outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setOutputs(outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.indexes.implementation.models.InputFieldMappingEntry> inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setInputs(inputs);
        }

        String name = obj.getName();
        entityRecognitionSkill.setName(name);

        String context = obj.getContext();
        entityRecognitionSkill.setContext(context);

        String description = obj.getDescription();
        entityRecognitionSkill.setDescription(description);

        Boolean includeTypelessEntities = obj.areTypelessEntitiesIncluded();
        entityRecognitionSkill.setIncludeTypelessEntities(includeTypelessEntities);

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillLanguage defaultLanguageCode =
                EntityRecognitionSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            entityRecognitionSkill.setDefaultLanguageCode(defaultLanguageCode);
        }

        if (obj.getCategories() != null) {
            List<com.azure.search.documents.indexes.implementation.models.EntityCategory> categories =
                obj.getCategories().stream().map(EntityCategoryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setCategories(categories);
        }

        Double minimumPrecision = obj.getMinimumPrecision();
        entityRecognitionSkill.setMinimumPrecision(minimumPrecision);
        return entityRecognitionSkill;
    }

    private EntityRecognitionSkillConverter() {
    }
}
