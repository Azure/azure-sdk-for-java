// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The EntitiesTask model.
 */
@Fluent
public final class EntitiesTask {
    /*
     * The parameters property.
     */
    private EntitiesTaskParameters parameters;

    /**
     * Get the parameters property: The parameters property.
     *
     * @return the parameters value.
     */
    public EntitiesTaskParameters getParameters() {
        return this.parameters;
    }

    /**
     * Set the parameters property: The parameters property.
     *
     * @param parameters the parameters value to set.
     * @return the EntitiesTask object itself.
     */
    public EntitiesTask setParameters(EntitiesTaskParameters parameters) {
        this.parameters = parameters;
        return this;
    }
}
