/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Previewed feature information.
 */
public class FeatureProperties {
    /**
     * Gets or sets the state of the previewed feature.
     */
    private String state;

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public String state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the FeatureProperties object itself.
     */
    public FeatureProperties withState(String state) {
        this.state = state;
        return this;
    }

}
