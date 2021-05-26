// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.FormRecognizerServiceVersion;
import com.azure.core.annotation.Fluent;

/**
 * The configurable options to pass when creating a composed model.
 *
 * This class is introduced since {@link FormRecognizerServiceVersion#V2_1}.
 */
@Fluent
public final class CreateComposedModelOptions {
    private String modelName;

    /**
     * Get the optional model name defined by the user.
     *
     * @return the modelName.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Set the optional model name defined by the user.
     *
     * @param modelName the user defined model name to set.
     *
     * @return the updated {@code CreateComposedModelOptions} value.
     */
    public CreateComposedModelOptions setModelName(final String modelName) {
        this.modelName = modelName;
        return this;
    }
}
