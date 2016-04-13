/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.AzureAuthenticated;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.implementation.SubscriptionsImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class Azure {
    public static Subscriptions authenticate(ServiceClientCredentials credentials) {
        return new SubscriptionsImpl(new SubscriptionClientImpl(credentials));
    }

    public static AzureAuthenticated authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new AzureAuthenticatedImpl(credentials, subscriptionId);
    }
}
