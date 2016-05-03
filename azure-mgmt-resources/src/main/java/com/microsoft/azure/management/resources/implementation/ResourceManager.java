/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.*;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigureBase;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class ResourceManager {
    private final RestClient restClient;
    // The sdk clients
    private SubscriptionClientImpl subscriptionClient;
    // The collections
    private Subscriptions subscriptions;
    private Tenants tenants;

    public static Configure configure() {
        return new AzureConfigureImpl();
    }

    public static ResourceManager authenticate(ServiceClientCredentials credentials) {
        return new ResourceManager(credentials);
    }

    public static ResourceManager authenticate(RestClient restClient) {
        return new ResourceManager(restClient);
    }

    public interface Configure extends AzureConfigureBase<Configure> {
        ResourceManager authenticate(ServiceClientCredentials credentials);
    }

    public interface Subscription {
        ResourceGroups resourceGroups();
        GenericResources genericResources();
        Deployments deployments();
        Deployments.InGroup deployments(String resourceGroupName);
    }

    private ResourceManager(ServiceClientCredentials credentials) {
        this.restClient = new RestClient
                .Builder("https://management.azure.com")
                .withCredentials(credentials)
                .build();
    }

    private ResourceManager(RestClient restClient) {
        this.restClient = restClient;
    }

    public Subscriptions subscriptions() {
        if (subscriptions == null) {
            subscriptions = new SubscriptionsImpl(subscriptionClient());
        }
        return subscriptions;
    }

    public Tenants tenants() {
        if (tenants == null) {
            tenants = new TenantsImpl(subscriptionClient());
        }
        return tenants;
    }

    public ResourceManager.Subscription withSubscription(String subscriptionId) {
        return new AzureSubscriptionImpl(restClient, subscriptionId);
    }

    private SubscriptionClientImpl subscriptionClient() {
        if (subscriptionClient == null) {
            subscriptionClient = new SubscriptionClientImpl(restClient);
        }
        return subscriptionClient;
    }
}
