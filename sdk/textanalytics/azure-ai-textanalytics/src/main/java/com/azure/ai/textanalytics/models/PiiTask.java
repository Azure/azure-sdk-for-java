// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/** The PiiTask model. */
@Fluent
public final class PiiTask {
    /*
     * The parameters property.
     */
    private PiiTaskParameters parameters;

    /**
     * Get the parameters property: The parameters property.
     *
     * @return the parameters value.
     */
    public PiiTaskParameters getParameters() {
        return this.parameters;
    }

    /**
     * Set the parameters property: The parameters property.
     *
     * @param parameters the parameters value to set.
     * @return the PiiTask object itself.
     */
    public PiiTask setParameters(PiiTaskParameters parameters) {
        this.parameters = parameters;
        return this;
    }
}
