// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for Features ListAll.
 */
public final class FeaturesListAllSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/
     * listSubscriptionFeatures.json
     */
    /**
     * Sample code: List subscription Features.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void listSubscriptionFeatures(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient().getFeatures().listAll(com.azure.core.util.Context.NONE);
    }
}
