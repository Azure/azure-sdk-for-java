// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for Features List.
 */
public final class FeaturesListSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/
     * listProviderFeatures.json
     */
    /**
     * Sample code: List provider Features.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void listProviderFeatures(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient().getFeatures().list("Resource Provider Namespace", com.azure.core.util.Context.NONE);
    }
}
