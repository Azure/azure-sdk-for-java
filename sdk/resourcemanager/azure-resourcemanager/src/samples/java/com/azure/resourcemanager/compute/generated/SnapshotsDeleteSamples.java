// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.generated;

/**
 * Samples for Snapshots Delete.
 */
public final class SnapshotsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/compute/resource-manager/Microsoft.Compute/DiskRP/stable/2025-01-02/examples/snapshotExamples/
     * Snapshot_Delete.json
     */
    /**
     * Sample code: Delete a snapshot.
     * 
     * @param azure The entry point for accessing resource management APIs in Azure.
     */
    public static void deleteASnapshot(com.azure.resourcemanager.AzureResourceManager azure) {
        azure.virtualMachines()
            .manager()
            .serviceClient()
            .getSnapshots()
            .delete("myResourceGroup", "mySnapshot", com.azure.core.util.Context.NONE);
    }
}
