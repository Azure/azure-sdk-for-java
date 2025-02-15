// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.logic.generated;

/**
 * Samples for IntegrationServiceEnvironmentNetworkHealth Get.
 */
public final class IntegrationServiceEnvironmentNetworkHealthGetSamples {
    /*
     * x-ms-original-file: specification/logic/resource-manager/Microsoft.Logic/stable/2019-05-01/examples/
     * IntegrationServiceEnvironments_NetworkHealth.json
     */
    /**
     * Sample code: Gets the integration service environment network health.
     * 
     * @param manager Entry point to LogicManager.
     */
    public static void
        getsTheIntegrationServiceEnvironmentNetworkHealth(com.azure.resourcemanager.logic.LogicManager manager) {
        manager.integrationServiceEnvironmentNetworkHealths()
            .getWithResponse("testResourceGroup", "testIntegrationServiceEnvironment",
                com.azure.core.util.Context.NONE);
    }
}
