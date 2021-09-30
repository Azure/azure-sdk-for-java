// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Options that may be passed using build model APIs on Document Administration client.
 */
@Fluent
public final class BuildModelOptions {
    private String description;

    /*
     * A case-sensitive prefix string to filter documents in the source path
     * for training.
     */
    private String prefix;

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
     * @return the BuildDocumentModelOptions object itself.
     */
    public BuildModelOptions setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the case-sensitive prefix string to filter
     * documents in the source path for training.
     *
     * @return the case-sensitive prefix string to filter documents for training.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Set the case-sensitive prefix string to filter documents in the source path for training.
     *
     * @param prefix the prefix value to set.
     * @return the BuildDocumentModelOptions object itself.
     */
    public BuildModelOptions setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }
}
