// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for SubscriptionFeatureRegistrations Delete.
 */
public final class SubscriptionFeatureRegistrationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/
     * FeatureRegistration/SubscriptionFeatureRegistrationDELETE.json
     */
    /**
     * Sample code: Deletes a feature registration.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void deletesAFeatureRegistration(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient()
            .getSubscriptionFeatureRegistrations()
            .deleteWithResponse("subscriptionFeatureRegistrationGroupTestRG", "testFeature",
                com.azure.core.util.Context.NONE);
    }
}
