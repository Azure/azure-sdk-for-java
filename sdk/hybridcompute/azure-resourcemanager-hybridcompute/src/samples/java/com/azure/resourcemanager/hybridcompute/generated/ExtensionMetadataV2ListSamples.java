// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridcompute.generated;

/**
 * Samples for ExtensionMetadataV2 List.
 */
public final class ExtensionMetadataV2ListSamples {
    /*
     * x-ms-original-file:
     * specification/hybridcompute/resource-manager/Microsoft.HybridCompute/preview/2025-02-19-preview/examples/
     * extension/ExtensionMetadataV2_List.json
     */
    /**
     * Sample code: GET a list of extension metadata.
     * 
     * @param manager Entry point to HybridComputeManager.
     */
    public static void
        gETAListOfExtensionMetadata(com.azure.resourcemanager.hybridcompute.HybridComputeManager manager) {
        manager.extensionMetadataV2s()
            .list("EastUS", "microsoft.azure.monitor", "azuremonitorlinuxagent", com.azure.core.util.Context.NONE);
    }
}
