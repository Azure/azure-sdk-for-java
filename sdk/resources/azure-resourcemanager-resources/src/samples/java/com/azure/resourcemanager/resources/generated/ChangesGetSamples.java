// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for Changes Get.
 */
public final class ChangesGetSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/changes/stable/2022-05-01/examples/GetChange.json
     */
    /**
     * Sample code: GetChange.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void getChange(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.resourceChangeClient()
            .getChanges()
            .getWithResponse("resourceGroup1", "resourceProvider1", "resourceType1", "resourceName1",
                "1d58d72f-0719-4a48-9228-b7ea682885bf", com.azure.core.util.Context.NONE);
    }
}
