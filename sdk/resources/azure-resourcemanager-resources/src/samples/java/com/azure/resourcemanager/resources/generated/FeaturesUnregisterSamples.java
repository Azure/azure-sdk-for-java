// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for Features Unregister.
 */
public final class FeaturesUnregisterSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/unregisterFeature
     * .json
     */
    /**
     * Sample code: Register feature.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void registerFeature(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient()
            .getFeatures()
            .unregisterWithResponse("Resource Provider Namespace", "feature", com.azure.core.util.Context.NONE);
    }
}
