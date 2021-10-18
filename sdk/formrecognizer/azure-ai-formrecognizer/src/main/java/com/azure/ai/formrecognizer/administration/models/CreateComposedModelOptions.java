// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * The configurable options to pass when creating a composed model.
 */
@Fluent
public final class CreateComposedModelOptions {
    private String description;

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
}
