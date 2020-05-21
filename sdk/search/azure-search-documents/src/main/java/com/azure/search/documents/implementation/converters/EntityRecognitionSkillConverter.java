// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.EntityCategory;
import com.azure.search.documents.models.EntityRecognitionSkill;
import com.azure.search.documents.models.EntityRecognitionSkillLanguage;
import com.azure.search.documents.models.InputFieldMappingEntry;
import com.azure.search.documents.models.OutputFieldMappingEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.EntityRecognitionSkill} and
 * {@link EntityRecognitionSkill}.
 */
public final class EntityRecognitionSkillConverter {
    private static final ClientLogger LOGGER = new ClientLogger(EntityRecognitionSkillConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.EntityRecognitionSkill} to
     * {@link EntityRecognitionSkill}.
     */
    public static EntityRecognitionSkill map(com.azure.search.documents.implementation.models.EntityRecognitionSkill obj) {
        if (obj == null) {
            return null;
        }
        EntityRecognitionSkill entityRecognitionSkill = new EntityRecognitionSkill();

        if (obj.getOutputs() != null) {
            List<OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        entityRecognitionSkill.setName(_name);

        String _context = obj.getContext();
        entityRecognitionSkill.setContext(_context);

        String _description = obj.getDescription();
        entityRecognitionSkill.setDescription(_description);

        Boolean _includeTypelessEntities = obj.isIncludeTypelessEntities();
        entityRecognitionSkill.setIncludeTypelessEntities(_includeTypelessEntities);

        if (obj.getDefaultLanguageCode() != null) {
            EntityRecognitionSkillLanguage _defaultLanguageCode =
                EntityRecognitionSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            entityRecognitionSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }

        if (obj.getCategories() != null) {
            List<EntityCategory> _categories =
                obj.getCategories().stream().map(EntityCategoryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setCategories(_categories);
        }

        Double _minimumPrecision = obj.getMinimumPrecision();
        entityRecognitionSkill.setMinimumPrecision(_minimumPrecision);
        return entityRecognitionSkill;
    }

    /**
     * Maps from {@link EntityRecognitionSkill} to
     * {@link com.azure.search.documents.implementation.models.EntityRecognitionSkill}.
     */
    public static com.azure.search.documents.implementation.models.EntityRecognitionSkill map(EntityRecognitionSkill obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.EntityRecognitionSkill entityRecognitionSkill =
            new com.azure.search.documents.implementation.models.EntityRecognitionSkill();

        if (obj.getOutputs() != null) {
            List<com.azure.search.documents.implementation.models.OutputFieldMappingEntry> _outputs =
                obj.getOutputs().stream().map(OutputFieldMappingEntryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setOutputs(_outputs);
        }

        if (obj.getInputs() != null) {
            List<com.azure.search.documents.implementation.models.InputFieldMappingEntry> _inputs =
                obj.getInputs().stream().map(InputFieldMappingEntryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setInputs(_inputs);
        }

        String _name = obj.getName();
        entityRecognitionSkill.setName(_name);

        String _context = obj.getContext();
        entityRecognitionSkill.setContext(_context);

        String _description = obj.getDescription();
        entityRecognitionSkill.setDescription(_description);

        Boolean _includeTypelessEntities = obj.isIncludeTypelessEntities();
        entityRecognitionSkill.setIncludeTypelessEntities(_includeTypelessEntities);

        if (obj.getDefaultLanguageCode() != null) {
            com.azure.search.documents.implementation.models.EntityRecognitionSkillLanguage _defaultLanguageCode =
                EntityRecognitionSkillLanguageConverter.map(obj.getDefaultLanguageCode());
            entityRecognitionSkill.setDefaultLanguageCode(_defaultLanguageCode);
        }

        if (obj.getCategories() != null) {
            List<com.azure.search.documents.implementation.models.EntityCategory> _categories =
                obj.getCategories().stream().map(EntityCategoryConverter::map).collect(Collectors.toList());
            entityRecognitionSkill.setCategories(_categories);
        }

        Double _minimumPrecision = obj.getMinimumPrecision();
        entityRecognitionSkill.setMinimumPrecision(_minimumPrecision);
        return entityRecognitionSkill;
    }
}
