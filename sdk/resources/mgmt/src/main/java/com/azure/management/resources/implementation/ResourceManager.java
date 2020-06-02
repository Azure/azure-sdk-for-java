// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
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
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.resources.fluentcore.utils.Utils;
import com.azure.management.resources.models.FeatureClientBuilder;
import com.azure.management.resources.models.FeatureClientImpl;
import com.azure.management.resources.models.PolicyClientBuilder;
import com.azure.management.resources.models.PolicyClientImpl;
import com.azure.management.resources.models.ResourceManagementClientBuilder;
import com.azure.management.resources.models.ResourceManagementClientImpl;
import com.azure.management.resources.models.SubscriptionClientBuilder;
import com.azure.management.resources.models.SubscriptionClientImpl;

import java.util.Objects;

/**
 * Entry point to Azure resource management.
 */
public final class ResourceManager extends ManagerBase implements HasInner<ResourceManagementClientImpl> {
    // The sdk clients
    private final ResourceManagementClientImpl resourceManagementClient;
    private final FeatureClientImpl featureClient;
//    private final SubscriptionClientImpl subscriptionClientClient;
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
     * @param credential the credential to use
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
        private HttpPipeline httpPipeline;
        private AzureProfile profile;
        private SdkContext sdkContext;
        private SubscriptionClientImpl subscriptionClient;
        // The subscription less collections
        private Subscriptions subscriptions;
        private Tenants tenants;

        AuthenticatedImpl(HttpPipeline httpPipeline, AzureProfile profile) {
            this.httpPipeline = httpPipeline;
            this.profile = profile;
            this.sdkContext = new SdkContext();
            this.subscriptionClient = (new SubscriptionClientBuilder())
                    .pipeline(httpPipeline)
                    .host(profile.environment().getResourceManagerEndpoint())
                    .buildClient();
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
                .host(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient();

        this.featureClient = new FeatureClientBuilder()
                .pipeline(httpPipeline)
                .host(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient();

        // Unread in spot bugs
//        this.subscriptionClientClient = new SubscriptionClientBuilder()
//                .pipeline(restClient.getHttpPipeline())
//                .host(restClient.getBaseUrl().toString())
//                .buildClient();

        this.policyClient = new PolicyClientBuilder()
                .pipeline(httpPipeline)
                .host(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient();
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
