// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.machinelearning.generated;

import com.azure.resourcemanager.machinelearning.models.ManagedNetworkProvisionOptions;

/**
 * Samples for ManagedNetworkProvisions ProvisionManagedNetwork.
 */
public final class ManagedNetworkProvisionsProvisionManagedNetworkSamples {
    /*
     * x-ms-original-file:
     * specification/machinelearningservices/resource-manager/Microsoft.MachineLearningServices/stable/2024-04-01/
     * examples/ManagedNetwork/provision.json
     */
    /**
     * Sample code: Provision ManagedNetwork.
     * 
     * @param manager Entry point to MachineLearningManager.
     */
    public static void
        provisionManagedNetwork(com.azure.resourcemanager.machinelearning.MachineLearningManager manager) {
        manager.managedNetworkProvisions()
            .provisionManagedNetwork("test-rg", "aml-workspace-name",
                new ManagedNetworkProvisionOptions().withIncludeSpark(false), com.azure.core.util.Context.NONE);
    }
}
