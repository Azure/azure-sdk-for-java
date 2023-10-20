// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.implementation.models.SentimentSkillV1;
import com.azure.search.documents.indexes.implementation.models.SentimentSkillV3;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/** Text analytics positive-negative sentiment analysis, scored as a floating point value in a range of zero to 1. */
@Fluent
public final class SentimentSkill extends SearchIndexerSkill {

    private static final ClientLogger LOGGER = new ClientLogger(SentimentSkill.class);

    /*
     * Identifies the concrete type of the skill.
     */
    private final SentimentSkillVersion version;

    private final SentimentSkillV1 v1Skill;
    private final SentimentSkillV3 v3Skill;

    SentimentSkill(SentimentSkillV1 v1Skill) {
        super(v1Skill.getInputs(), v1Skill.getOutputs());
        this.version = SentimentSkillVersion.V1;
        this.v1Skill = v1Skill;
        this.v3Skill = null;
    }

    SentimentSkill(SentimentSkillV3 v3Skill) {
        super(v3Skill.getInputs(), v3Skill.getOutputs());
        this.version = SentimentSkillVersion.V3;
        this.v1Skill = null;
        this.v3Skill = v3Skill;
    }

    /**
     * Creates an instance of SentimentSkill class.
     * <p>
     * The instance of SentimentSkill uses {@link SentimentSkillVersion#V1}, to set the specific version of the skill
     * use {@link #SentimentSkill(List, List, SentimentSkillVersion)}.
     *
     * @param inputs the inputs value to set.
     * @param outputs the outputs value to set.
     * @deprecated Use {@link #SentimentSkill(List, List, SentimentSkillVersion)} as {@link SentimentSkillVersion#V1} is
     * deprecated. See
     * <a href="https://learn.microsoft.com/azure/search/cognitive-search-skill-deprecated">skill deprecation</a> for
     * more information.
     */
    @Deprecated
    public SentimentSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs) {
        this(inputs, outputs, SentimentSkillVersion.V1);
    }

    /**
     * Creates an instance of SentimentSkill class.
     *
     * @param inputs the inputs value to set.
     * @param outputs the outputs value to set.
     * @param version the SentimentSkillVersion value to set.
     * @throws NullPointerException If {@code version} is null.
     */
    public SentimentSkill(List<InputFieldMappingEntry> inputs, List<OutputFieldMappingEntry> outputs,
        SentimentSkillVersion version) {
        super(inputs, outputs);
        this.version = Objects.requireNonNull(version, "'version' cannot be null.");
        if (version == SentimentSkillVersion.V1) {
            this.v1Skill = new SentimentSkillV1(inputs, outputs);
            this.v3Skill = null;
        } else {
            this.v1Skill = null;
            this.v3Skill = new SentimentSkillV3(inputs, outputs);
        }
    }

    /**
     * Gets the version of the {@link SentimentSkill}.
     *
     * @return The version of the {@link SentimentSkill}.
     */
    public SentimentSkillVersion getSkillVersion() {
        return this.version;
    }

    /**
     * Get the defaultLanguageCode property: A value indicating which language code to use. Default is en.
     *
     * @return the defaultLanguageCode value.
     */
    public SentimentSkillLanguage getDefaultLanguageCode() {
        return (v1Skill != null)
            ? v1Skill.getDefaultLanguageCode()
            : SentimentSkillLanguage.fromString(v3Skill.getDefaultLanguageCode());
    }

    /**
     * Set the defaultLanguageCode property: A value indicating which language code to use. Default is en.
     *
     * @param defaultLanguageCode the defaultLanguageCode value to set.
     * @return the SentimentSkill object itself.
     */
    public SentimentSkill setDefaultLanguageCode(SentimentSkillLanguage defaultLanguageCode) {
        if (v1Skill != null) {
            v1Skill.setDefaultLanguageCode(defaultLanguageCode);
        } else {
            v3Skill.setDefaultLanguageCode((defaultLanguageCode == null) ? null : defaultLanguageCode.toString());
        }

        return this;
    }

    /**
     * Get the includeOpinionMining property: If set to true, the skill output will include information from Text
     * Analytics for opinion mining, namely targets (nouns or verbs) and their associated assessment (adjective) in the
     * text. Default is false.
     *
     * @return the includeOpinionMining value.
     */
    public Boolean isOpinionMiningIncluded() {
        return (v1Skill != null) ? null : v3Skill.isIncludeOpinionMining();
    }

    /**
     * Set the opinionMiningIncluded property: If set to true, the skill output will include information from Text
     * Analytics for opinion mining, namely targets (nouns or verbs) and their associated assessment (adjective) in the
     * text. Default is false.
     *
     * @param opinionMiningIncluded the opinionMiningIncluded value to set.
     * @return the SentimentSkill object itself.
     * @throws IllegalArgumentException If {@code opinionMiningIncluded} is supplied when {@link #getSkillVersion()} is
     *     {@link SentimentSkillVersion#V1}.
     */
    public SentimentSkill setOpinionMiningIncluded(Boolean opinionMiningIncluded) {
        if (opinionMiningIncluded != null && version == SentimentSkillVersion.V1) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("SentimentSkill using V1 doesn't support 'opinionMiningIncluded'."));
        }

        if (v3Skill != null) {
            v3Skill.setIncludeOpinionMining(opinionMiningIncluded);
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
     * @return the SentimentSkill object itself.
     * @throws IllegalArgumentException If {@code modelVersion} is supplied when {@link #getSkillVersion()} is {@link
     *     SentimentSkillVersion#V1}.
     */
    public SentimentSkill setModelVersion(String modelVersion) {
        if (modelVersion != null && version == SentimentSkillVersion.V1) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("SentimentSkill using V1 doesn't support 'modelVersion'."));
        }

        if (v3Skill != null) {
            v3Skill.setModelVersion(modelVersion);
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SentimentSkill setName(String name) {
        super.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SentimentSkill setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public SentimentSkill setContext(String context) {
        super.setContext(context);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return (v1Skill != null) ? v1Skill.toJson(jsonWriter) : v3Skill.toJson(jsonWriter);
    }
}
