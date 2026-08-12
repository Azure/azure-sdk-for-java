// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

import com.azure.resourcemanager.resources.fluent.models.SubscriptionFeatureRegistrationInner;
import com.azure.resourcemanager.resources.models.SubscriptionFeatureRegistrationProperties;

/**
 * Samples for SubscriptionFeatureRegistrations CreateOrUpdate.
 */
public final class SubscriptionFeatureRegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/
     * FeatureRegistration/SubscriptionFeatureRegistrationPUT.json
     */
    /**
     * Sample code: Creates a feature registration.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void createsAFeatureRegistration(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient()
            .getSubscriptionFeatureRegistrations()
            .createOrUpdateWithResponse("subscriptionFeatureRegistrationGroupTestRG", "testFeature",
                new SubscriptionFeatureRegistrationInner()
                    .withProperties(new SubscriptionFeatureRegistrationProperties()),
                com.azure.core.util.Context.NONE);
    }
}
