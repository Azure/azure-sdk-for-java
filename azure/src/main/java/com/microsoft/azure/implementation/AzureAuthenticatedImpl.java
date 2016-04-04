/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.AzureAuthenticated;
import com.microsoft.azure.Subscription;
import com.microsoft.azure.management.resources.client.SubscriptionClient;
import com.microsoft.azure.management.resources.client.implementation.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.collection.Subscriptions;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class AzureAuthenticatedImpl implements AzureAuthenticated {
    private ServiceClientCredentials credentials;
    private SubscriptionClient subscriptionClient;

    AzureAuthenticatedImpl(ServiceClientCredentials credentials) {
        this.credentials = credentials;
        this.subscriptionClient = new SubscriptionClientImpl(credentials);
    }

    @Override
    public Subscriptions subscriptions() {
        return subscriptionClient.subscriptions();
    }

    @Override
    public Subscription subscription(String subscriptionId) {
        return new SubscriptionImpl(credentials, subscriptionId);
    }
}
