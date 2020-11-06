// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/** The EntitiesTaskParameters model. */
@Fluent
public final class EntitiesTaskParameters {
    /*
     * The model-version property.
     */
    private String modelVersion;

    /**
     * Get the modelVersion property: The model-version property.
     *
     * @return the modelVersion value.
     */
    public String getModelVersion() {
        return this.modelVersion;
    }

    /**
     * Set the modelVersion property: The model-version property.
     *
     * @param modelVersion the modelVersion value to set.
     * @return the EntitiesTaskParameters object itself.
     */
    public EntitiesTaskParameters setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }
}
