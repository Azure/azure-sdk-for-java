/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.AzureResourceAuthenticated;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.azure.management.resources.models.Subscription;
import com.microsoft.azure.management.resources.models.implementation.SubscriptionImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.io.IOException;

public class AzureResourceAuthenticatedImpl implements AzureResourceAuthenticated {
    private ServiceClientCredentials credentials;
    private SubscriptionClientImpl subscriptionClient;

    AzureResourceAuthenticatedImpl(ServiceClientCredentials credentials) {
        this.credentials = credentials;
        this.subscriptionClient = new SubscriptionClientImpl(credentials);
    }

    @Override
    public Subscriptions subscriptions() throws IOException, CloudException {
        return new SubscriptionsImpl(subscriptionClient);
    }
}
