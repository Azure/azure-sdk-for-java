// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for Changes List.
 */
public final class ChangesListSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Resources/changes/stable/2022-05-01/examples/ListChanges.json
     */
    /**
     * Sample code: ListChanges.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void listChanges(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.resourceChangeClient()
            .getChanges()
            .list("resourceGroup1", "resourceProvider1", "resourceType1", "resourceName1", null, null,
                com.azure.core.util.Context.NONE);
    }
}
