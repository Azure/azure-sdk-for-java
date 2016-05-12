/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

/**
 * Defines values for ServiceProviderProvisioningState.
 */
public final class ServiceProviderProvisioningState {
    /** Static value NotProvisioned for ServiceProviderProvisioningState. */
    public static final String NOT_PROVISIONED = "NotProvisioned";

    /** Static value Provisioning for ServiceProviderProvisioningState. */
    public static final String PROVISIONING = "Provisioning";

    /** Static value Provisioned for ServiceProviderProvisioningState. */
    public static final String PROVISIONED = "Provisioned";

    /** Static value Deprovisioning for ServiceProviderProvisioningState. */
    public static final String DEPROVISIONING = "Deprovisioning";

    private ServiceProviderProvisioningState() {
    }
}
