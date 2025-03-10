// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appcontainers.generated;

/**
 * Samples for ContainerApps GetAuthToken.
 */
public final class ContainerAppsGetAuthTokenSamples {
    /*
     * x-ms-original-file:
     * specification/app/resource-manager/Microsoft.App/preview/2024-08-02-preview/examples/ContainerApps_GetAuthToken.
     * json
     */
    /**
     * Sample code: Get Container App Auth Token.
     * 
     * @param manager Entry point to ContainerAppsApiManager.
     */
    public static void
        getContainerAppAuthToken(com.azure.resourcemanager.appcontainers.ContainerAppsApiManager manager) {
        manager.containerApps().getAuthTokenWithResponse("rg", "testcontainerApp0", com.azure.core.util.Context.NONE);
    }
}
