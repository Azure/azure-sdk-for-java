// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks List.
 */
public final class ManagementLocksListSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_ListAtSubscriptionLevel.json
     */
    /**
     * Sample code: List management locks at subscription level.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void
        listManagementLocksAtSubscriptionLevel(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient().getManagementLocks().list(null, com.azure.core.util.Context.NONE);
    }
}
