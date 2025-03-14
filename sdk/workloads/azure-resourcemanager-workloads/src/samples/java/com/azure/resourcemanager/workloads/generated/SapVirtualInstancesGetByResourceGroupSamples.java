// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.workloads.generated;

/**
 * Samples for SapVirtualInstances GetByResourceGroup.
 */
public final class SapVirtualInstancesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/workloads/resource-manager/Microsoft.Workloads/stable/2023-04-01/examples/sapvirtualinstances/
     * SAPVirtualInstances_Get.json
     */
    /**
     * Sample code: SAPVirtualInstances_Get.
     * 
     * @param manager Entry point to WorkloadsManager.
     */
    public static void sAPVirtualInstancesGet(com.azure.resourcemanager.workloads.WorkloadsManager manager) {
        manager.sapVirtualInstances()
            .getByResourceGroupWithResponse("test-rg", "X00", com.azure.core.util.Context.NONE);
    }
}
