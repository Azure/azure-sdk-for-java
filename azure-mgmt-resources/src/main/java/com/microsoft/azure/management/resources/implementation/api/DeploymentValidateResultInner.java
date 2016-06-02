/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Information from validate template deployment response.
 */
public class DeploymentValidateResultInner {
    /**
     * Gets or sets validation error.
     */
    private ResourceManagementErrorWithDetails error;

    /**
     * Gets or sets the template deployment properties.
     */
    private DeploymentPropertiesExtended properties;

    /**
     * Get the error value.
     *
     * @return the error value
     */
    public ResourceManagementErrorWithDetails error() {
        return this.error;
    }

    /**
     * Set the error value.
     *
     * @param error the error value to set
     * @return the DeploymentValidateResultInner object itself.
     */
    public DeploymentValidateResultInner withError(ResourceManagementErrorWithDetails error) {
        this.error = error;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public DeploymentPropertiesExtended properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the DeploymentValidateResultInner object itself.
     */
    public DeploymentValidateResultInner withProperties(DeploymentPropertiesExtended properties) {
        this.properties = properties;
        return this;
    }

}
