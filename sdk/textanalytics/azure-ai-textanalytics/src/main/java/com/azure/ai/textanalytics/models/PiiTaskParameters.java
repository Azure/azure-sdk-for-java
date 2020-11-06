// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/** The PiiTaskParameters model. */
@Fluent
public final class PiiTaskParameters {
    /*
     * The domain property.
     */
    private PiiTaskParametersDomain domain;

    /*
     * The model-version property.
     */
    private String modelVersion;

    /**
     * Get the domain property: The domain property.
     *
     * @return the domain value.
     */
    public PiiTaskParametersDomain getDomain() {
        return this.domain;
    }

    /**
     * Set the domain property: The domain property.
     *
     * @param domain the domain value to set.
     * @return the PiiTaskParameters object itself.
     */
    public PiiTaskParameters setDomain(PiiTaskParametersDomain domain) {
        this.domain = domain;
        return this;
    }

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
     * @return the PiiTaskParameters object itself.
     */
    public PiiTaskParameters setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }
}
