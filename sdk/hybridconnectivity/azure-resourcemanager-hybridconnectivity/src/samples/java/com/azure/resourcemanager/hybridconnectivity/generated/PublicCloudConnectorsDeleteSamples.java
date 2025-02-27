// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.hybridconnectivity.generated;

/**
 * Samples for PublicCloudConnectors Delete.
 */
public final class PublicCloudConnectorsDeleteSamples {
    /*
     * x-ms-original-file: 2024-12-01/PublicCloudConnectors_Delete.json
     */
    /**
     * Sample code: PublicCloudConnectors_Delete.
     * 
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void
        publicCloudConnectorsDelete(com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager.publicCloudConnectors()
            .deleteByResourceGroupWithResponse("rgpublicCloud", "skcfyjvflkhibdywjay",
                com.azure.core.util.Context.NONE);
    }
}
