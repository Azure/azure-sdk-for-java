/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Deployment filter.
 */
public class DeploymentExtendedFilter {
    /**
     * Gets or sets the provisioning state.
     */
    private String provisioningState;

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the DeploymentExtendedFilter object itself.
     */
    public DeploymentExtendedFilter withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

}
