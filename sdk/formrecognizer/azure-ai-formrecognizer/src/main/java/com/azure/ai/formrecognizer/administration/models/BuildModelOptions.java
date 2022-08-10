// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Options that may be passed using build model APIs on Document Administration client.
 */
@Fluent
public final class BuildModelOptions {
    private String description;

    private Map<String, String> tags;

    private String modelId;

    /**
     * Get the model description.
     *
     * @return the model description value.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the model description.
     *
     * @param description the model description value to set.
     * @return the BuildModelOptions object itself.
     */
    public BuildModelOptions setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the user defined attributes associated with the model.
     *
     * @return the tags value.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Set the user defined attributes associated with the model.
     *
     * @param tags the tags value to set.
     * @return the BuildModelOptions object itself.
     */
    public BuildModelOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the unique model identifier for the model.
     *
     * @return the modelId
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * Set the unique model identifier for the model. If not specified, a model ID will be created for you.
     *
     * @param modelId a unique model identifier
     * @return the BuildModelOptions object itself.
     */
    public BuildModelOptions setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }
}
