// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Generated code

package com.azure.resourcemanager.resources.generated;

/**
 * Samples for SubscriptionFeatureRegistrations List.
 */
public final class SubscriptionFeatureRegistrationsListSamples {
    /*
     * x-ms-original-file:
     * specification/resources/resource-manager/Microsoft.Features/features/stable/2021-07-01/examples/
     * FeatureRegistration/SubscriptionFeatureRegistrationLISTALL.json
     */
    /**
     * Sample code: Gets a list of feature registrations.
     * 
     * @param manager Entry point to ResourceManager.
     */
    public static void getsAListOfFeatureRegistrations(com.azure.resourcemanager.resources.ResourceManager manager) {
        manager.featureClient().getSubscriptionFeatureRegistrations().list(com.azure.core.util.Context.NONE);
    }
}
