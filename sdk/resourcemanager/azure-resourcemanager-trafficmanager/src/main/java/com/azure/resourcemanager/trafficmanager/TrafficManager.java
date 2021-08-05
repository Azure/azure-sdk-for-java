// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.trafficmanager.fluent.TrafficManagerManagementClient;
import com.azure.resourcemanager.trafficmanager.implementation.TrafficManagerManagementClientBuilder;
import com.azure.resourcemanager.trafficmanager.implementation.TrafficManagerProfilesImpl;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfiles;

/** Entry point to Azure traffic manager management. */
public final class TrafficManager extends Manager<TrafficManagerManagementClient> {
    // Collections
    private TrafficManagerProfiles profiles;

    /**
     * Get a Configurable instance that can be used to create {@link TrafficManager} with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new TrafficManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
     *
     * @param credential the credentials to use
     * @param profile the profile to use
     * @return the TrafficManager
     */
    public static TrafficManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
     *
     * @param httpPipeline the RestClient to be used for API calls.
     * @param profile the profile to use
     * @return the TrafficManager
     */
    private static TrafficManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new TrafficManager(httpPipeline, profile);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing traffic manager management API entry points that work across subscriptions
         */
        TrafficManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public TrafficManager authenticate(TokenCredential credential, AzureProfile profile) {
            return TrafficManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private TrafficManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new TrafficManagerManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
    }

    /** @return entry point to traffic manager profile management */
    public TrafficManagerProfiles profiles() {
        if (this.profiles == null) {
            this.profiles = new TrafficManagerProfilesImpl(this);
        }
        return this.profiles;
    }
}
