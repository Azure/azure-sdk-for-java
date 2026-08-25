// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for SubscriptionFeatureRegistrations Get.
 */
public final class SubscriptionFeatureRegistrationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/
     * FeatureRegistration/SubscriptionFeatureRegistrationGET.json
     */
    /**
     * Sample code: Gets a feature registration.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void getsAFeatureRegistration(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient()
            .getSubscriptionFeatureRegistrations()
            .getWithResponse("subscriptionFeatureRegistrationGroupTestRG", "testFeature",
                com.azure.core.util.Context.NONE);
    }
}
