// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/** The KeyPhrasesTaskParameters model. */
@Fluent
public final class KeyPhrasesTaskParameters {
    /*
     * The model-version property.
     */
    // TODO: currently, service does not set their default values for model version, we temporally set the default
    // value to 'latest' until service correct it. https://github.com/Azure/azure-sdk-for-java/issues/17625
    private String modelVersion = "latest";

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
     * @return the KeyPhrasesTaskParameters object itself.
     */
    public KeyPhrasesTaskParameters setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }
}
