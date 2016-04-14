/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.Tenants;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class AzureResourceManager {
    public static AzureResourceManager.Authenticated authenticate(ServiceClientCredentials credentials) {
        return new AzureAuthenticatedImpl(credentials);
    }

    public static AzureResourceManager.Subscription authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new AzureSubscriptionImpl(credentials, subscriptionId);
    }

    public interface Authenticated {
        Subscription subscription(String subscriptionId);
        Subscriptions subscriptions();
        Tenants tenants();
    }

    public interface Subscription {
        ResourceGroups resourceGroups();
        GenericResources genericResources();
    }
}
