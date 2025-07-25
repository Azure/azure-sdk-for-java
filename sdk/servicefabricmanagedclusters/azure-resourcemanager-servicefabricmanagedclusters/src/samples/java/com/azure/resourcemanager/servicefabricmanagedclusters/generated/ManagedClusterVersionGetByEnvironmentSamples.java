// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.servicefabricmanagedclusters.generated;

import com.azure.resourcemanager.servicefabricmanagedclusters.models.ManagedClusterVersionEnvironment;

/**
 * Samples for ManagedClusterVersion GetByEnvironment.
 */
public final class ManagedClusterVersionGetByEnvironmentSamples {
    /*
     * x-ms-original-file: 2025-03-01-preview/ManagedClusterVersionGetByEnvironment_example.json
     */
    /**
     * Sample code: Get cluster version by environment.
     * 
     * @param manager Entry point to ServiceFabricManagedClustersManager.
     */
    public static void getClusterVersionByEnvironment(
        com.azure.resourcemanager.servicefabricmanagedclusters.ServiceFabricManagedClustersManager manager) {
        manager.managedClusterVersions()
            .getByEnvironmentWithResponse("eastus", ManagedClusterVersionEnvironment.WINDOWS, "7.2.477.9590",
                com.azure.core.util.Context.NONE);
    }
}
