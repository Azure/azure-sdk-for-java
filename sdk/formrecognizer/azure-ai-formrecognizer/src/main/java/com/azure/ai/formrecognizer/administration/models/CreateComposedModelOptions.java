// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * The configurable options to pass when creating a composed model.
 */
@Fluent
public final class CreateComposedModelOptions {
    private String description;
    private Map<String, String> tags;
    private String modelId;

    /**
     * Get the optional model description defined by the user.
     *
     * @return the modelName.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the optional model description defined by the user.
     *
     * @param description the user defined model description to set.
     *
     * @return the updated {@code CreateComposedModelOptions} value.
     */
    public CreateComposedModelOptions setDescription(final String description) {
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
     * @return the CreateComposedModelOptions object itself.
     */
    public CreateComposedModelOptions setTags(Map<String, String> tags) {
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
     * @return the CreateComposedModelOptions object itself.
     */
    public CreateComposedModelOptions setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }
}
