// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/** The KeyPhrasesTask model. */
@Fluent
public final class KeyPhrasesTask {
    /*
     * The parameters property.
     */
    private KeyPhrasesTaskParameters parameters = new KeyPhrasesTaskParameters();

    /**
     * Get the parameters property: The parameters property.
     *
     * @return the parameters value.
     */
    public KeyPhrasesTaskParameters getParameters() {
        return this.parameters;
    }

    /**
     * Set the parameters property: The parameters property.
     *
     * @param parameters the parameters value to set.
     * @return the KeyPhrasesTask object itself.
     */
    public KeyPhrasesTask setParameters(KeyPhrasesTaskParameters parameters) {
        this.parameters = parameters;
        return this;
    }
}
