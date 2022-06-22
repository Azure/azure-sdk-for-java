// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillV1;
import com.azure.search.documents.indexes.implementation.models.EntityRecognitionSkillV3;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Text analytics entity recognition. */
@Fluent
public final class EntityRecognitionSkill extends SearchIndexerSkill {

    private static final ClientLogger LOGGER = new ClientLogger(EntityRecognitionSkill.class);

    /*
     * Identifies the concrete type of the skill.
     */
    private final EntityRecognitionSkillVersion version;

    private final EntityRecognitionSkillV1 v1Skill;
    private final EntityRecognitionSkillV3 v3Skill;

    EntityRecognitionSkill(EntityRecognitionSkillV1 v1Skill) {
        super(v1Skill.getInputs(), v1Skill.getOutputs());
        this.version = EntityRecognitionSkillVersion.V1;
        this.v1Skill = v1Skill;
        this.v3Skill = null;
    }

    EntityRecognitionSkill(EntityRecognitionSkillV3 v3Skill) {
        super(v3Skill.getInputs(), v3Skill.getOutputs());
        this.version = EntityRecognitionSkillVersion.V3;
        this.v1Skill = null;
        this.v3Skill = v3Skill;
    }

    /**
     * Creates an instance of EntityRecognitionSkill class.
     *
     * @param inputs the inputs value to set.
     * @param outputs the outputs value to set.
     */
    public EntityRecognitionSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        this(inputs, outputs, EntityRecognitionSkillVersion.V1);
    }

    /**
     * Creates an instance of EntityRecognitionSkill class.
     *
     * @param inputs the inputs value to set.
     * @param outputs the outputs value to set.
     * @param version the EntityRecognitionSkillVersion value to set.
     * @throws NullPointerException If {@code version} is null.
     */
    public EntityRecognitionSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs,
        EntityRecognitionSkillVersion version) {
        super(inputs, outputs);
        this.version = Objects.requireNonNull(version, "'version' cannot be null.");

        if (version == EntityRecognitionSkillVersion.V1) {
            this.v1Skill = new EntityRecognitionSkillV1(inputs, outputs);
            this.v3Skill = null;
        } else {
            this.v1Skill = null;
            this.v3Skill = new EntityRecognitionSkillV3(inputs, outputs);
        }
    }

    /**
     * Gets the version of the {@link EntityRecognitionSkill}.
     *
     * @return The version of the {@link EntityRecognitionSkill}.
     */
    public EntityRecognitionSkillVersion getSkillVersion() {
        return this.version;
    }

    /**
     * Get the categories property: A list of entity categories that should be extracted.
     *
     * @return the categories value.
     */
    public List<EntityCategory> getCategories() {
        if (v1Skill != null) {
            return v1Skill.getCategories();
        } else {
            List<String> categories = v3Skill.getCategories();
            if (categories == null) {
                return null;
            } else {
                return categories.stream().map(EntityCategory::fromString).collect(Collectors.toList());
            }
        }
    }

    /**
     * Set the categories property: A list of entity categories that should be extracted.
     *
     * @param categories the categories value to set.
     * @return the EntityRecognitionSkill object itself.
     */
    public EntityRecognitionSkill setCategories(List<EntityCategory> categories) {
        if (v1Skill != null) {
            v1Skill.setCategories(categories);
        } else {
            if (categories == null) {
                v3Skill.setCategories(null);
            } else {
                v3Skill.setCategories(categories.stream().map(EntityCategory::toString).collect(Collectors.toList()));
            }
        }

        return this;
    }

    /**
     * Get the defaultLanguageCode property: A value indicating which language code to use. Default is en.
     *
     * @return the defaultLanguageCode value.
     */
    public EntityRecognitionSkillLanguage getDefaultLanguageCode() {
        return (v1Skill != null)
            ? v1Skill.getDefaultLanguageCode()
            : EntityRecognitionSkillLanguage.fromString(v3Skill.getDefaultLanguageCode());
    }

    /**
     * Set the defaultLanguageCode property: A value indicating which language code to use. Default is en.
     *
     * @param defaultLanguageCode the defaultLanguageCode value to set.
     * @return the EntityRecognitionSkill object itself.
     */
    public EntityRecognitionSkill setDefaultLanguageCode(EntityRecognitionSkillLanguage defaultLanguageCode) {
        if (v1Skill != null) {
            v1Skill.setDefaultLanguageCode(defaultLanguageCode);
        } else {
            v3Skill.setDefaultLanguageCode((defaultLanguageCode == null) ? null : defaultLanguageCode.toString());
        }

        return this;
    }

    /**
     * Get the includeTypelessEntities property: Determines whether or not to include entities which are well known but
     * don't conform to a pre-defined type. If this configuration is not set (default), set to null or set to false,
     * entities which don't conform to one of the pre-defined types will not be surfaced.
     *
     * @return the includeTypelessEntities value.
     */
    public Boolean areTypelessEntitiesIncluded() {
        return (v1Skill != null) ? v1Skill.isIncludeTypelessEntities() : null;
    }

    /**
     * Set the includeTypelessEntities property: Determines whether or not to include entities which are well known but
     * don't conform to a pre-defined type. If this configuration is not set (default), set to null or set to false,
     * entities which don't conform to one of the pre-defined types will not be surfaced.
     *
     * @param includeTypelessEntities the includeTypelessEntities value to set.
     * @return the EntityRecognitionSkill object itself.
     * @throws IllegalArgumentException If {@code includeTypelessEntities} is supplied when {@link #getSkillVersion()}
     *     is {@link EntityRecognitionSkillVersion#V3}.
     */
    public EntityRecognitionSkill setTypelessEntitiesIncluded(Boolean includeTypelessEntities) {
        if (includeTypelessEntities != null && version == EntityRecognitionSkillVersion.V3) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(
                    "EntityRecognitionSkill using V3 doesn't support 'includeTypelessEntities'."));
        }

        if (v1Skill != null) {
            v1Skill.setIncludeTypelessEntities(includeTypelessEntities);
        }

        return this;
    }

    /**
     * Get the minimumPrecision property: A value between 0 and 1 that be used to only include entities whose confidence
     * score is greater than the value specified. If not set (default), or if explicitly set to null, all entities will
     * be included.
     *
     * @return the minimumPrecision value.
     */
    public Double getMinimumPrecision() {
        return (v1Skill != null) ? v1Skill.getMinimumPrecision() : v3Skill.getMinimumPrecision();
    }

    /**
     * Set the minimumPrecision property: A value between 0 and 1 that be used to only include entities whose confidence
     * score is greater than the value specified. If not set (default), or if explicitly set to null, all entities will
     * be included.
     *
     * @param minimumPrecision the minimumPrecision value to set.
     * @return the EntityRecognitionSkill object itself.
     */
    public EntityRecognitionSkill setMinimumPrecision(Double minimumPrecision) {
        if (v1Skill != null) {
            v1Skill.setMinimumPrecision(minimumPrecision);
        } else {
            v3Skill.setMinimumPrecision(minimumPrecision);
        }

        return this;
    }

    /**
     * Get the modelVersion property: The version of the model to use when calling the Text Analytics service. It will
     * default to the latest available when not specified. We recommend you do not specify this value unless absolutely
     * necessary.
     *
     * @return the modelVersion value.
     */
    public String getModelVersion() {
        return (v1Skill != null) ? null : v3Skill.getModelVersion();
    }

    /**
     * Set the modelVersion property: The version of the model to use when calling the Text Analytics service. It will
     * default to the latest available when not specified. We recommend you do not specify this value unless absolutely
     * necessary.
     *
     * @param modelVersion the modelVersion value to set.
     * @return the EntityRecognitionSkill object itself.
     * @throws IllegalArgumentException If {@code modelVersion} is supplied when {@link #getSkillVersion()} is {@link
     *     EntityRecognitionSkillVersion#V1}.
     */
    public EntityRecognitionSkill setModelVersion(String modelVersion) {
        if (modelVersion != null && version == EntityRecognitionSkillVersion.V1) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("EntityRecognitionSkill using V1 doesn't support 'modelVersion'."));
        }

        if (v3Skill != null) {
            v3Skill.setModelVersion(modelVersion);
        }

        return this;
    }

    /**
     * Set the categories property: A list of entity categories that should be extracted.
     *
     * @param categories the categories value to set.
     * @return the EntityRecognitionSkill object itself.
     */
    public EntityRecognitionSkill setCategories(EntityCategory... categories) {
        return setCategories((categories == null) ? null : java.util.Arrays.asList(categories));
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        return (v1Skill != null) ? v1Skill.toJson(jsonWriter) : v3Skill.toJson(jsonWriter);
    }
}
