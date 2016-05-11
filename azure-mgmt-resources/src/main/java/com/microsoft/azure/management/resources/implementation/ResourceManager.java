/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.management.resources.*;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.implementation.api.FeatureClientImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionClientImpl;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class ResourceManager {
    // The sdk clients
    private final ResourceManagementClientImpl resourceManagementClient;
    private final FeatureClientImpl featureClient;
    // The collections
    private ResourceGroups resourceGroups;
    private GenericResources genericResources;
    private Deployments deployments;
    private Features features;
    private Providers providers;

    public static ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials) {
        return new AuthenticatedImpl(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build());
    }

    public static ResourceManager.Authenticated authenticate(RestClient restClient) {
        return new AuthenticatedImpl(restClient);
    }

    public static Configurable configure() {
        return new ResourceManager.ConfigurableImpl();
    }

    public interface Configurable extends AzureConfigurable<Configurable> {
        ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials);
    }

    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public ResourceManager.Authenticated authenticate(ServiceClientCredentials credentials) {
            return ResourceManager.authenticate(buildRestClient(credentials));
        }
    }

    public interface Authenticated {
        Tenants tenants();
        Subscriptions subscriptions();
        ResourceManager withSubscription(String subscriptionId);
    }

    private static final class AuthenticatedImpl implements Authenticated {
        private RestClient restClient;
        private SubscriptionClientImpl subscriptionClient;
        // The subscription less collections
        private Subscriptions subscriptions;
        private Tenants tenants;

        AuthenticatedImpl(RestClient restClient) {
            this.restClient = restClient;
            this.subscriptionClient = new SubscriptionClientImpl(restClient);
        }

        public Subscriptions subscriptions() {
            if (subscriptions == null) {
                subscriptions = new SubscriptionsImpl(subscriptionClient.subscriptions());
            }
            return subscriptions;
        }

        public Tenants tenants() {
            if (tenants == null) {
                tenants = new TenantsImpl(subscriptionClient.tenants());
            }
            return tenants;
        }

        @Override
        public ResourceManager withSubscription(String subscriptionId) {
           return new ResourceManager(restClient, subscriptionId);
        }
    }

    private ResourceManager(RestClient restClient, String subscriptionId) {
        this.resourceManagementClient = new ResourceManagementClientImpl(restClient);
        this.resourceManagementClient.setSubscriptionId(subscriptionId);
        this.featureClient = new FeatureClientImpl(restClient);
        this.featureClient.setSubscriptionId(subscriptionId);
    }

    public ResourceGroups resourceGroups() {
        if (resourceGroups == null) {
            resourceGroups = new ResourceGroupsImpl(resourceManagementClient);
        }
        return resourceGroups;
    }

    public GenericResources genericResources() {
        if (genericResources == null) {
            genericResources = new GenericResourcesImpl(resourceManagementClient);
        }
        return genericResources;
    }

    public Deployments deployments() {
        if (deployments == null) {
            deployments = new DeploymentsImpl(
                    resourceManagementClient.deployments(),
                    resourceManagementClient.deploymentOperations(),
                    resourceGroups());
        }
        return deployments;
    }

    public Features features() {
        if (features == null) {
            features = new FeaturesImpl(featureClient.features());
        }
        return features;
    }

    public Providers providers() {
        if (providers == null) {
            providers = new ProvidersImpl(resourceManagementClient.providers());
        }
        return providers;
    }
}
