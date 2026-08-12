// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for ManagementLocks ListByScope.
 */
public final class ManagementLocksListByScopeSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ManagementLocks_ListAtScope.json
     */
    /**
     * Sample code: List management locks at scope.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void listManagementLocksAtScope(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient()
            .getManagementLocks()
            .listByScope("subscriptions/subscriptionId", null, com.azure.core.util.Context.NONE);
    }
}
