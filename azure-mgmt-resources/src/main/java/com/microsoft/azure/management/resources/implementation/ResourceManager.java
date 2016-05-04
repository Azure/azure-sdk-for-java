/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.*;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class ResourceManager {
    private RestClient restClient;
    private String subscriptionId;
    // The sdk clients
    private ResourceManagementClientImpl resourceManagementClient;
    // The collections
    ResourceGroups resourceGroups;
    GenericResources genericResources;
    Deployments deployments;
    Deployments.InGroup deploymentsInGroup;

    public static Configure configure() {
        return new ResourceManager().new ConfigurableImpl();
    }

    public static ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials) {
        return (new ResourceManager(credentials)).new AuthenticatedImpl();
    }

    public static ResourceManager.Authenticated authenticate(RestClient restClient) {
        return (new ResourceManager(restClient)).new AuthenticatedImpl();
    }

    public interface Configure extends AzureConfigurable<Configure> {
        ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials);
    }

    class ConfigurableImpl extends AzureConfigurableImpl<Configure> implements Configure {
        public ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials) {
            buildRestClient(credentials);
            return ResourceManager.authenticate(restClient);
        }
    }

    public interface Authenticated {
        Tenants tenants();
        Subscriptions subscriptions();
        ResourceManager useSubscription(String subscriptionId);
    }

    class AuthenticatedImpl implements Authenticated {
        private SubscriptionClientImpl subscriptionClient;
        // The subscription less collections
        private Subscriptions subscriptions;
        private Tenants tenants;

        public AuthenticatedImpl() {}

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
           ResourceManager.this.subscriptionId =  subscriptionId;
           return ResourceManager.this;
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

    private ResourceManager() {}

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

    public Deployments.InGroup deployments(ResourceGroup resourceGroup) {
        if (deploymentsInGroup == null) {
            deploymentsInGroup = new DeploymentsInGroupImpl(deployments(), resourceGroup);
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
}
