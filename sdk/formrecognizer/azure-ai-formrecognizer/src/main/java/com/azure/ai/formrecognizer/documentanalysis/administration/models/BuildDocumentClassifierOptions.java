// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.administration.models;

import com.azure.core.annotation.Fluent;

/**
 * Options that may be passed to build document classifier models.
 */
@Fluent
public final class BuildDocumentClassifierOptions {
    private String description;

    private String classifierId;

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
     * @return the BuildDocumentClassifierOptions object itself.
     */
    public BuildDocumentClassifierOptions setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the unique document classifier name.
     *
     * @return the classifierId value.
     */
    public String getClassifierId() {
        return classifierId;
    }

    /**
     * Set the unique document classifier name.
     *
     * @param classifierId the classifierId value to set.
     * @return the BuildDocumentClassifierOptions object itself.
     */
    public BuildDocumentClassifierOptions setClassifierId(String classifierId) {
        this.classifierId = classifierId;
        return this;
    }
}
