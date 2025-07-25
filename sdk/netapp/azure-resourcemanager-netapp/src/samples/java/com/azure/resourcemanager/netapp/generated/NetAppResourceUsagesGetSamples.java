// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.netapp.generated;

/**
 * Samples for NetAppResourceUsages Get.
 */
public final class NetAppResourceUsagesGetSamples {
    /*
     * x-ms-original-file:
     * specification/netapp/resource-manager/Microsoft.NetApp/stable/2025-03-01/examples/Usages_Get.json
     */
    /**
     * Sample code: Usages_Get.
     * 
     * @param manager Entry point to NetAppFilesManager.
     */
    public static void usagesGet(com.azure.resourcemanager.netapp.NetAppFilesManager manager) {
        manager.netAppResourceUsages()
            .getWithResponse("eastus", "totalTibsPerSubscription", com.azure.core.util.Context.NONE);
    }
}
