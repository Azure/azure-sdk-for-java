// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks Delete.
 */
public final class ManagementLocksDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_DeleteAtResourceGroupLevel.json
     */
    /**
     * Sample code: Delete management lock at resource group level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void
        deleteManagementLockAtResourceGroupLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .deleteWithResponse("resourcegroupname", "testlock", com.azure.core.util.Context.NONE);
    }
}
