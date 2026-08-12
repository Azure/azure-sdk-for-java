// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks DeleteAtSubscriptionLevel.
 */
public final class ManagementLocksDeleteAtSubscriptionLevelSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_DeleteAtSubscriptionLevel.json
     */
    /**
     * Sample code: Delete management lock at subscription level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void
        deleteManagementLockAtSubscriptionLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .deleteAtSubscriptionLevelWithResponse("testlock", com.azure.core.util.Context.NONE);
    }
}
