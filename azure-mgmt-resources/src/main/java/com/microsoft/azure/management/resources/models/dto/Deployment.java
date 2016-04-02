/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.models.dto;


/**
 * Deployment operation parameters.
 */
public class Deployment {
    /**
     * Gets or sets the deployment properties.
     */
    private DeploymentProperties properties;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public DeploymentProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(DeploymentProperties properties) {
        this.properties = properties;
    }

}
