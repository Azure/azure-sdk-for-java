// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks DeleteAtResourceLevel.
 */
public final class ManagementLocksDeleteAtResourceLevelSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_DeleteAtResourceLevel.json
     */
    /**
     * Sample code: Delete management lock at resource level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void
        deleteManagementLockAtResourceLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .deleteAtResourceLevelWithResponse("resourcegroupname", "Microsoft.Storage", "parentResourcePath",
                "storageAccounts", "teststorageaccount", "testlock", com.azure.core.util.Context.NONE);
    }
}
