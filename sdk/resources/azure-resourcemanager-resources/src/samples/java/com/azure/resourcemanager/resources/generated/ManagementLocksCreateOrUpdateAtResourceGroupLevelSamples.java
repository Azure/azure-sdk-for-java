// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

import com.azure.resourcemanager.resources.fluent.models.ManagementLockObjectInner;
import com.azure.resourcemanager.resources.models.LockLevel;

/**
 * Samples for ManagementLocks CreateOrUpdateAtResourceGroupLevel.
 */
public final class ManagementLocksCreateOrUpdateAtResourceGroupLevelSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_CreateOrUpdateAtResourceGroupLevel.json
     */
    /**
     * Sample code: Create management lock at resource group level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void
        createManagementLockAtResourceGroupLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .createOrUpdateAtResourceGroupLevelWithResponse("resourcegroupname", "testlock",
                new ManagementLockObjectInner().withLevel(LockLevel.READ_ONLY), com.azure.core.util.Context.NONE);
    }
}
