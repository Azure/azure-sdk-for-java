// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.resources.implementation.DeploymentsImpl;
import com.azure.resourcemanager.resources.implementation.FeaturesImpl;
import com.azure.resourcemanager.resources.implementation.GenericResourcesImpl;
import com.azure.resourcemanager.resources.implementation.PolicyAssignmentsImpl;
import com.azure.resourcemanager.resources.implementation.PolicyDefinitionsImpl;
import com.azure.resourcemanager.resources.implementation.ProvidersImpl;
import com.azure.resourcemanager.resources.implementation.ResourceGroupsImpl;
import com.azure.resourcemanager.resources.implementation.SubscriptionsImpl;
import com.azure.resourcemanager.resources.implementation.TenantsImpl;
import com.azure.resourcemanager.resources.models.Deployments;
import com.azure.resourcemanager.resources.models.Features;
import com.azure.resourcemanager.resources.models.GenericResources;
import com.azure.resourcemanager.resources.models.PolicyAssignments;
import com.azure.resourcemanager.resources.models.PolicyDefinitions;
import com.azure.resourcemanager.resources.models.Providers;
import com.azure.resourcemanager.resources.models.ResourceGroups;
import com.azure.resourcemanager.resources.models.Subscriptions;
import com.azure.resourcemanager.resources.models.Tenants;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.ManagerBase;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;

import java.util.Objects;

/**
 * Entry point to Azure resource management.
 */
public final class ResourceManager extends ManagerBase implements HasInner<ResourceManagementClient> {
    // The sdk clients
    private final ResourceManagementClient resourceManagementClient;
    private final FeatureClient featureClient;
    private final SubscriptionClient subscriptionClient;
    private final PolicyClient policyClient;
    // The collections
    private ResourceGroups resourceGroups;
    private GenericResources genericResources;
    private Deployments deployments;
    private Features features;
    private Providers providers;
    private PolicyDefinitions policyDefinitions;
    private PolicyAssignments policyAssignments;
    private Subscriptions subscriptions;
    private Tenants tenants;

    /**
     * Creates an instance of ResourceManager that exposes resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile used in resource management
     * @return the ResourceManager instance
     */
    public static ResourceManager.Authenticated authenticate(TokenCredential credential, AzureProfile profile) {
        return new AuthenticatedImpl(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of ResourceManager that exposes resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls
     * @param profile the profile used in resource management
     * @return the interface exposing resource management API entry points that work across subscriptions
     */
    public static ResourceManager.Authenticated authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new AuthenticatedImpl(httpPipeline, profile);
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
         * @param credential the credential to use
         * @param profile the profile used in resource management
         * @return the interface exposing resource management API entry points that work across subscriptions
         */
        ResourceManager.Authenticated authenticate(TokenCredential credential, AzureProfile profile);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public ResourceManager.Authenticated authenticate(TokenCredential credential, AzureProfile profile) {
            return ResourceManager.authenticate(buildHttpPipeline(credential, profile), profile);
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

        /**
         * Specifies to use subscription from {@link AzureProfile}. If no subscription provided, we will
         * try to set the only subscription if applicable returned by {@link Authenticated#subscriptions()}.
         *
         * @throws IllegalStateException when no subscription or more than one subscription found in the tenant.
         * @return the ResourceManager instance with entry points that work in a subscription
         */
        ResourceManager withDefaultSubscription();
    }

    /**
     * The implementation for Authenticated interface.
     */
    private static final class AuthenticatedImpl implements Authenticated {
        private final HttpPipeline httpPipeline;
        private AzureProfile profile;
        private SdkContext sdkContext;
        private final SubscriptionClient subscriptionClient;
        // The subscription less collections
        private Subscriptions subscriptions;
        private Tenants tenants;

        AuthenticatedImpl(HttpPipeline httpPipeline, AzureProfile profile) {
            this.httpPipeline = httpPipeline;
            this.profile = profile;
            this.sdkContext = new SdkContext();
            this.subscriptionClient = new SubscriptionClientBuilder()
                    .pipeline(httpPipeline)
                    .endpoint(profile.environment().getResourceManagerEndpoint())
                    .buildClient();
        }

        public Subscriptions subscriptions() {
            if (subscriptions == null) {
                subscriptions = new SubscriptionsImpl(subscriptionClient.getSubscriptions());
            }
            return subscriptions;
        }

        public Tenants tenants() {
            if (tenants == null) {
                tenants = new TenantsImpl(subscriptionClient.getTenants());
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
            Objects.requireNonNull(subscriptionId);
            profile = new AzureProfile(profile.tenantId(), subscriptionId, profile.environment());
            return new ResourceManager(httpPipeline, profile, sdkContext);
        }

        @Override
        public ResourceManager withDefaultSubscription() {
            if (profile.subscriptionId() == null) {
                String subscriptionId = Utils.defaultSubscription(this.subscriptions().list());
                profile = new AzureProfile(profile.tenantId(), subscriptionId, profile.environment());
            }
            return new ResourceManager(httpPipeline, profile, sdkContext);
        }
    }

    private ResourceManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(null, profile, sdkContext);
        super.withResourceManager(this);
        this.resourceManagementClient = new ResourceManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient();

        this.featureClient = new FeatureClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient();

        this.subscriptionClient = new SubscriptionClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .buildClient();

        this.policyClient = new PolicyClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient();
    }

    /**
     * @return the resource group management API entry point
     */
    public ResourceGroups resourceGroups() {
        if (resourceGroups == null) {
            resourceGroups = new ResourceGroupsImpl(this);
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
            features = new FeaturesImpl(featureClient.getFeatures());
        }
        return features;
    }

    /**
     * @return the resource provider management API entry point
     */
    public Providers providers() {
        if (providers == null) {
            providers = new ProvidersImpl(resourceManagementClient.getProviders());
        }
        return providers;
    }

    /**
     * @return the policy definition management API entry point
     */
    public PolicyDefinitions policyDefinitions() {
        if (policyDefinitions == null) {
            policyDefinitions = new PolicyDefinitionsImpl(policyClient.getPolicyDefinitions());
        }
        return policyDefinitions;
    }

    /**
     * @return the policy assignment management API entry point
     */
    public PolicyAssignments policyAssignments() {
        if (policyAssignments == null) {
            policyAssignments = new PolicyAssignmentsImpl(policyClient.getPolicyAssignments());
        }
        return policyAssignments;
    }

    /**
     * @return the subscription management API entry point
     */
    public Subscriptions subscriptions() {
        if (subscriptions == null) {
            subscriptions = new SubscriptionsImpl(subscriptionClient.getSubscriptions());
        }
        return subscriptions;
    }

    /**
     * @return the tenant management API entry point
     */
    public Tenants tenants() {
        if (tenants == null) {
            tenants = new TenantsImpl(subscriptionClient.getTenants());
        }
        return tenants;
    }

    @Override
    public ResourceManagementClient inner() {
        return this.resourceManagementClient;
    }
}
