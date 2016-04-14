/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.Tenants;
import com.microsoft.azure.management.resources.api.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureBaseImpl;
import com.microsoft.azure.management.resources.implementation.SubscriptionsImpl;
import com.microsoft.azure.management.resources.implementation.TenantsImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

final class AzureAuthenticatedImpl extends AzureBaseImpl<Azure.Authenticated>
        implements Azure.Authenticated {
    private ServiceClientCredentials credentials;
    private SubscriptionClientImpl subscriptionClient;

    AzureAuthenticatedImpl(ServiceClientCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Subscriptions subscriptions() {
        if (subscriptionClient == null) {
            subscriptionClient = new SubscriptionClientImpl(credentials);
        }
        return new SubscriptionsImpl(subscriptionClient);
    }

    @Override
    public Tenants tenants() {
        if (subscriptionClient == null) {
            subscriptionClient = new SubscriptionClientImpl(credentials);
        }
        return new TenantsImpl(subscriptionClient);
    }

    @Override
    public Azure.Subscription withSubscription(String subscriptionId) {
        return new AzureSubscriptionImpl(credentials, subscriptionId);
    }
}
