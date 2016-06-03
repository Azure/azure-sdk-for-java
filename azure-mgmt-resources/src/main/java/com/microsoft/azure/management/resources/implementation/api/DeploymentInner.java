/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Deployment operation parameters.
 */
public class DeploymentInner {
    /**
     * Gets or sets the deployment properties.
     */
    private DeploymentProperties properties;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public DeploymentProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the DeploymentInner object itself.
     */
    public DeploymentInner withProperties(DeploymentProperties properties) {
        this.properties = properties;
        return this;
    }

}
