// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for AuthorizationOperations List.
 */
public final class AuthorizationOperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Authorization/locks/stable/2017-04-01/examples/
     * ListProviderOperations.json
     */
    /**
     * Sample code: List provider operations.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void listProviderOperations(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.managementLockClient().getAuthorizationOperations().list(com.azure.core.util.Context.NONE);
    }
}
