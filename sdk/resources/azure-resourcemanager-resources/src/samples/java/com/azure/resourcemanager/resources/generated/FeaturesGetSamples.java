// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for Features Get.
 */
public final class FeaturesGetSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/getFeature.json
     */
    /**
     * Sample code: Get feature.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void getFeature(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient()
            .getFeatures()
            .getWithResponse("Resource Provider Namespace", "feature", com.azure.core.util.Context.NONE);
    }
}
