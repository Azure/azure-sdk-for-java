// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Options that may be passed when copying models into the target Form Recognizer resource.
 */
@Fluent
public final class CopyAuthorizationOptions {
    private String description;
    private Map<String, String> tags;

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
     * @return the CopyAuthorizationOptions object itself.
     */
    public CopyAuthorizationOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
