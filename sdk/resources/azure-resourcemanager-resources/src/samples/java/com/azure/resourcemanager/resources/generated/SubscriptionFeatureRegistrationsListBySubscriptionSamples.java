// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for SubscriptionFeatureRegistrations ListBySubscription.
 */
public final class SubscriptionFeatureRegistrationsListBySubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/
     * FeatureRegistration/SubscriptionFeatureRegistrationLIST.json
     */
    /**
     * Sample code: Gets a list of feature registrations.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void getsAListOfFeatureRegistrations(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient()
            .getSubscriptionFeatureRegistrations()
            .listBySubscription("subscriptionFeatureRegistrationGroupTestRG", com.azure.core.util.Context.NONE);
    }
}
