// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicessiterecovery.generated;

/**
 * Samples for ReplicationProtectedItems FailoverCancel.
 */
public final class ReplicationProtectedItemsFailoverCancelSamples {
    /*
     * x-ms-original-file:
     * specification/recoveryservicessiterecovery/resource-manager/Microsoft.RecoveryServices/stable/2025-01-01/examples
     * /ReplicationProtectedItems_FailoverCancel.json
     */
    /**
     * Sample code: Execute cancel failover.
     * 
     * @param manager Entry point to SiteRecoveryManager.
     */
    public static void
        executeCancelFailover(com.azure.resourcemanager.recoveryservicessiterecovery.SiteRecoveryManager manager) {
        manager.replicationProtectedItems()
            .failoverCancel("resourceGroupPS1", "vault1", "cloud1", "cloud_6d224fc6-f326-5d35-96de-fbf51efb3179",
                "f8491e4f-817a-40dd-a90c-af773978c75b", com.azure.core.util.Context.NONE);
    }
}
