// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appplatform.generated;

/**
 * Samples for BuildService ListBuildResults.
 */
public final class BuildServiceListBuildResultsSamples {
    /*
     * x-ms-original-file: specification/appplatform/resource-manager/Microsoft.AppPlatform/stable/2023-12-01/examples/
     * BuildService_ListBuildResults.json
     */
    /**
     * Sample code: BuildService_ListBuildResults.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void buildServiceListBuildResults(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.springServices()
            .manager()
            .serviceClient()
            .getBuildServices()
            .listBuildResults("myResourceGroup", "myservice", "default", "mybuild", com.azure.core.util.Context.NONE);
    }
}
