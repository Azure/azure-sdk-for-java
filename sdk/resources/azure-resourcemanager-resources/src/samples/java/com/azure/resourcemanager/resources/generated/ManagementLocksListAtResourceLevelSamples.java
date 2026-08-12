// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks ListAtResourceLevel.
 */
public final class ManagementLocksListAtResourceLevelSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_ListAtResourceLevel.json
     */
    /**
     * Sample code: List management locks at resource level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void listManagementLocksAtResourceLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .listAtResourceLevel("resourcegroupname", "Microsoft.Storage", "parentResourcePath", "storageAccounts",
                "teststorageaccount", null, com.azure.core.util.Context.NONE);
    }
}
