// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridnetwork.generated;

/**
 * Samples for ProxyArtifact Get.
 */
public final class ProxyArtifactGetSamples {
    /*
     * x-ms-original-file:
     * specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PureProxyArtifact
     * /ArtifactGet.json
     */
    /**
     * Sample code: Get an artifact overview.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getAnArtifactOverview(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.proxyArtifacts()
            .get("TestResourceGroup", "TestPublisher", "TestArtifactStoreName", "fedrbac",
                com.azure.core.util.Context.NONE);
    }
}
