// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks ListByResourceGroup.
 */
public final class ManagementLocksListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_ListAtResourceGroupLevel.json
     */
    /**
     * Sample code: List management groups at resource group level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void
        listManagementGroupsAtResourceGroupLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .listByResourceGroup("resourcegroupname", null, com.azure.core.util.Context.NONE);
    }
}
