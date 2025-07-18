// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.dashboard.generated;

/**
 * Samples for IntegrationFabrics Get.
 */
public final class IntegrationFabricsGetSamples {
    /*
     * x-ms-original-file: 2024-11-01-preview/IntegrationFabrics_Get.json
     */
    /**
     * Sample code: IntegrationFabrics_Get.
     * 
     * @param manager Entry point to DashboardManager.
     */
    public static void integrationFabricsGet(com.azure.resourcemanager.dashboard.DashboardManager manager) {
        manager.integrationFabrics()
            .getWithResponse("myResourceGroup", "myWorkspace", "sampleIntegration", com.azure.core.util.Context.NONE);
    }
}
