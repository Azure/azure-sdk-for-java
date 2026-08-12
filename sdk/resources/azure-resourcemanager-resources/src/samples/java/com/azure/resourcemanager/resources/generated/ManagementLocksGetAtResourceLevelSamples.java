// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks GetAtResourceLevel.
 */
public final class ManagementLocksGetAtResourceLevelSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_GetAtResourceLevel.json
     */
    /**
     * Sample code: Get management lock at resource level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void getManagementLockAtResourceLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .getAtResourceLevelWithResponse("resourcegroupname", "Microsoft.Storage", "parentResourcePath",
                "storageAccounts", "teststorageaccount", "testlock", com.azure.core.util.Context.NONE);
    }
}
