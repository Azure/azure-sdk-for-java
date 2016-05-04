/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.Tenants;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureAuthenticatedImpl
        implements AzureResourceManager.Authenticated {
    private final RestClient restClient;
    private SubscriptionClientImpl subscriptionClient;

    AzureAuthenticatedImpl(ServiceClientCredentials credentials) {
        this.restClient = new RestClient
                .Builder("https://management.azure.com")
                .withCredentials(credentials)
                .build();
    }

    AzureAuthenticatedImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Subscriptions subscriptions() {
        if (subscriptionClient == null) {
            subscriptionClient = new SubscriptionClientImpl(restClient);
        }
        return new SubscriptionsImpl(subscriptionClient);
    }

    @Override
    public Tenants tenants() {
        if (subscriptionClient == null) {
            subscriptionClient = new SubscriptionClientImpl(restClient);
        }
        return new TenantsImpl(subscriptionClient);
    }

    @Override
    public AzureResourceManager.Subscription withSubscription(String subscriptionId) {
        return new AzureSubscriptionImpl(restClient, subscriptionId);
    }
}
