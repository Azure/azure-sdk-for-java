/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.*;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigureBase;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class ResourceManager {
    private final RestClient restClient;
    private String subscriptionId;
    // The sdk clients
    private ResourceManagementClientImpl resourceManagementClient;
    // The collections
    ResourceGroups resourceGroups;
    GenericResources genericResources;
    Deployments deployments;
    Deployments.InGroup deploymentsInGroup;

    public static Configure configure() {
        return new AzureConfigureImpl();
    }

    public static ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials) {
        ResourceManager resourceManager = new ResourceManager(credentials);
        return resourceManager.createAuthenticatedImpl();
    }

    public static ResourceManager.Authenticated authenticate(RestClient restClient) {
        ResourceManager resourceManager = new ResourceManager(restClient);
        return resourceManager.createAuthenticatedImpl();
    }

    public interface Configure extends AzureConfigureBase<Configure> {
        ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials);
    }

    public interface Authenticated {
        Tenants tenants();
        Subscriptions subscriptions();
        ResourceManager useSubscription(String subscriptionId);
    }

    class AuthenticatedImpl implements Authenticated {
        private final RestClient restClient;
        private SubscriptionClientImpl subscriptionClient;
        // The subscription less collections
        private Subscriptions subscriptions;
        private Tenants tenants;

        public AuthenticatedImpl(RestClient restClient) {
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

        public ResourceManager useSubscription(String subscriptionId) {
           return new ResourceManager(restClient, subscriptionId);
        }

        private SubscriptionClientImpl subscriptionClient() {
            if (subscriptionClient == null) {
                subscriptionClient = new SubscriptionClientImpl(restClient);
            }
            return subscriptionClient;
        }
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

    private ResourceManager(RestClient restClient, String subscriptionId) {
        this.restClient = restClient;
        this.subscriptionId = subscriptionId;
    }

    public ResourceGroups resourceGroups() {
        if (resourceGroups == null) {
            resourceGroups = new ResourceGroupsImpl(resourceManagementClient());
        }
        return resourceGroups;
    }

    public GenericResources genericResources() {
        if (genericResources == null) {
            genericResources = new GenericResourcesImpl(resourceManagementClient());
        }
        return genericResources;
    }

    public Deployments deployments() {
        if (deployments == null) {
            deployments = new DeploymentsImpl(resourceManagementClient());
        }
        return deployments;
    }

    public Deployments.InGroup deployments(String resourceGroupName) {
        if (deploymentsInGroup == null) {
            deploymentsInGroup = new DeploymentsInGroupImpl(resourceManagementClient(), resourceGroupName);
        }
        return deploymentsInGroup;
    }

    private ResourceManagementClientImpl resourceManagementClient() {
        if (resourceManagementClient == null) {
            resourceManagementClient = new ResourceManagementClientImpl(restClient);
            resourceManagementClient.setSubscriptionId(subscriptionId);
        }
        return resourceManagementClient;
    }

    private AuthenticatedImpl createAuthenticatedImpl() {
        return new AuthenticatedImpl(this.restClient);
    }
}
