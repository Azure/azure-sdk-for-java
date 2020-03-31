/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.resources.Deployments;
import com.azure.management.resources.Features;
import com.azure.management.resources.GenericResources;
import com.azure.management.resources.PolicyAssignments;
import com.azure.management.resources.PolicyDefinitions;
import com.azure.management.resources.Providers;
import com.azure.management.resources.ResourceGroups;
import com.azure.management.resources.Subscriptions;
import com.azure.management.resources.Tenants;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.ManagerBase;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.resources.models.FeatureClientBuilder;
import com.azure.management.resources.models.FeatureClientImpl;
import com.azure.management.resources.models.PolicyClientBuilder;
import com.azure.management.resources.models.PolicyClientImpl;
import com.azure.management.resources.models.ResourceManagementClientBuilder;
import com.azure.management.resources.models.ResourceManagementClientImpl;
import com.azure.management.resources.models.SubscriptionClientBuilder;
import com.azure.management.resources.models.SubscriptionClientImpl;

/**
 * Entry point to Azure resource management.
 */
public final class ResourceManager extends ManagerBase implements HasInner<ResourceManagementClientImpl> {
    // The sdk clients
    private final ResourceManagementClientImpl resourceManagementClient;
    private final FeatureClientImpl featureClient;
    private final SubscriptionClientImpl subscriptionClientClient;
    private final PolicyClientImpl policyClient;
    // The collections
    private ResourceGroups resourceGroups;
    private GenericResources genericResources;
    private Deployments deployments;
    private Features features;
    private Providers providers;
    private PolicyDefinitions policyDefinitions;
    private PolicyAssignments policyAssignments;

    /**
     * Creates an instance of ResourceManager that exposes resource management API entry points.
     *
     * @param credential the credentials to use
     * @return the ResourceManager instance
     */
    public static ResourceManager.Authenticated authenticate(AzureTokenCredential credential) {
        return new AuthenticatedImpl(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient());
    }

    /**
     * Creates an instance of ResourceManager that exposes resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls
     * @return the interface exposing resource management API entry points that work across subscriptions
     */
    public static ResourceManager.Authenticated authenticate(RestClient restClient) {
        return new AuthenticatedImpl(restClient);
    }

    /**
     * Get a Configurable instance that can be used to create ResourceManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new ResourceManager.ConfigurableImpl();
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ResourceManager that exposes resource management API entry points.
         *
         * @param credentials the credentials to use
         * @return the interface exposing resource management API entry points that work across subscriptions
         */
        ResourceManager.Authenticated authenticate(AzureTokenCredential credentials);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public ResourceManager.Authenticated authenticate(AzureTokenCredential credential) {
            return ResourceManager.authenticate(buildRestClient(credential));
        }
    }

    /**
     * The interface exposing resource management API entry points that work across subscriptions.
     */
    public interface Authenticated {
        /**
         * @return the entry point to tenant management API.
         */
        Tenants tenants();

        /**
         * @return the entry point to subscription management API.
         */
        Subscriptions subscriptions();

        /**
         * Specifies sdk context for resource manager.
         * 
         * @param sdkContext the sdk context
         * @return the authenticated itself for chaining
         */
        Authenticated withSdkContext(SdkContext sdkContext);

        /**
         * Specifies a subscription to expose resource management API entry points that work in a subscription.
         *
         * @param subscriptionId the subscription UUID
         * @return the ResourceManager instance with entry points that work in a subscription
         */
        ResourceManager withSubscription(String subscriptionId);
    }

    /**
     * The implementation for Authenticated interface.
     */
    private static final class AuthenticatedImpl implements Authenticated {
        private RestClient restClient;
        private SdkContext sdkContext;
        private SubscriptionClientImpl subscriptionClient;
        // The subscription less collections
        private Subscriptions subscriptions;
        private Tenants tenants;

        AuthenticatedImpl(RestClient restClient) {
            this.restClient = restClient;
            this.sdkContext = new SdkContext();
            this.subscriptionClient = (new SubscriptionClientBuilder())
                    .pipeline(restClient.getHttpPipeline())
                    .host(restClient.getBaseUrl().toString())
                    .build();
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
        public AuthenticatedImpl withSdkContext(SdkContext sdkContext) {
            this.sdkContext = sdkContext;
            return this;
        }

        @Override
        public ResourceManager withSubscription(String subscriptionId) {
            return new ResourceManager(restClient, subscriptionId, sdkContext);
        }
    }

    private ResourceManager(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        super(null, subscriptionId, sdkContext);
        super.setResourceManager(this);
        this.resourceManagementClient = new ResourceManagementClientBuilder()
                .pipeline(restClient.getHttpPipeline())
                .host(restClient.getBaseUrl().toString())
                .subscriptionId(subscriptionId)
                .build();

        this.featureClient = new FeatureClientBuilder()
                .pipeline(restClient.getHttpPipeline())
                .host(restClient.getBaseUrl().toString())
                .subscriptionId(subscriptionId)
                .build();

        this.subscriptionClientClient = new SubscriptionClientBuilder()
                .pipeline(restClient.getHttpPipeline())
                .host(restClient.getBaseUrl().toString())
                .build();

        this.policyClient = new PolicyClientBuilder()
                .pipeline(restClient.getHttpPipeline())
                .host(restClient.getBaseUrl().toString())
                .subscriptionId(subscriptionId)
                .build();
    }

    /**
     * @return the resource group management API entry point
     */
    public ResourceGroups resourceGroups() {
        if (resourceGroups == null) {
            resourceGroups = new ResourceGroupsImpl(resourceManagementClient);
        }
        return resourceGroups;
    }

    /**
     * @return the generic resource management API entry point
     */
    public GenericResources genericResources() {
        if (genericResources == null) {
            genericResources = new GenericResourcesImpl(this);
        }
        return genericResources;
    }

    /**
     * @return the deployment management API entry point
     */
    public Deployments deployments() {
        if (deployments == null) {
            deployments = new DeploymentsImpl(this);
        }
        return deployments;
    }

    /**
     * @return the feature management API entry point
     */
    public Features features() {
        if (features == null) {
            features = new FeaturesImpl(featureClient.features());
        }
        return features;
    }

    /**
     * @return the resource provider management API entry point
     */
    public Providers providers() {
        if (providers == null) {
            providers = new ProvidersImpl(resourceManagementClient.providers());
        }
        return providers;
    }

    /**
     * @return the policy definition management API entry point
     */
    public PolicyDefinitions policyDefinitions() {
        if (policyDefinitions == null) {
            policyDefinitions = new PolicyDefinitionsImpl(policyClient.policyDefinitions());
        }
        return policyDefinitions;
    }

    /**
     * @return the policy assignment management API entry point
     */
    public PolicyAssignments policyAssignments() {
        if (policyAssignments == null) {
            policyAssignments = new PolicyAssignmentsImpl(policyClient.policyAssignments());
        }
        return policyAssignments;
    }

    @Override
    public ResourceManagementClientImpl inner() {
        return this.resourceManagementClient;
    }
}
