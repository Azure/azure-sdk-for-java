/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.AzureAuthenticated;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.implementation.SubscriptionsImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.io.IOException;

public class AzureAuthenticatedImpl implements AzureAuthenticated {
    private ServiceClientCredentials credentials;
    private Subscriptions subscriptions;

    AzureAuthenticatedImpl(ServiceClientCredentials credentials) throws IOException, CloudException {
        this.credentials = credentials;
        SubscriptionClientImpl sClient = new SubscriptionClientImpl(credentials);
        this.subscriptions = new SubscriptionsImpl(sClient);
    }

    @Override
    public Subscriptions subscriptions() {
        return this.subscriptions;
    }

    @Override
    public Subscription subscription(String subscriptionId) throws IOException, CloudException {
        return new SubscriptionImpl(credentials, subscriptionId);
    }
}
