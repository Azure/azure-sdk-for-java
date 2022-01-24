// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Options that may be passed when copying models into the target Form Recognizer resource.
 */
@Fluent
public final class CopyAuthorizationOptions {
    private String description;

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
     * @return the CopyAuthorizationOptions object itself.
     */
    public CopyAuthorizationOptions setDescription(String description) {
        this.description = description;
        return this;
    }
}
