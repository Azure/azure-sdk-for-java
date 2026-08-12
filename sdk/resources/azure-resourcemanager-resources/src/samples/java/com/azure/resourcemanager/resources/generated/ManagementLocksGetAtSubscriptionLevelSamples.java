// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks GetAtSubscriptionLevel.
 */
public final class ManagementLocksGetAtSubscriptionLevelSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_GetAtSubscriptionLevel.json
     */
    /**
     * Sample code: Get management lock at subscription level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void
        getManagementLockAtSubscriptionLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .getAtSubscriptionLevelWithResponse("testlock", com.azure.core.util.Context.NONE);
    }
}
