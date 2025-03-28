// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.devcenter.generated;

/**
 * Samples for Pools Get.
 */
public final class PoolsGetSamples {
    /*
     * x-ms-original-file:
     * specification/devcenter/resource-manager/Microsoft.DevCenter/stable/2024-02-01/examples/Pools_GetUnhealthyStatus.
     * json
     */
    /**
     * Sample code: Pools_GetUnhealthyStatus.
     * 
     * @param manager Entry point to DevCenterManager.
     */
    public static void poolsGetUnhealthyStatus(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.pools().getWithResponse("rg1", "DevProject", "DevPool", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/devcenter/resource-manager/Microsoft.DevCenter/stable/2024-02-01/examples/Pools_Get.json
     */
    /**
     * Sample code: Pools_Get.
     * 
     * @param manager Entry point to DevCenterManager.
     */
    public static void poolsGet(com.azure.resourcemanager.devcenter.DevCenterManager manager) {
        manager.pools().getWithResponse("rg1", "DevProject", "DevPool", com.azure.core.util.Context.NONE);
    }
}
