/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfiles;

/**
 * Entry point to Azure traffic manager management.
 */
public final class TrafficManager extends Manager<TrafficManager, TrafficManagerManagementClientImpl> {
    // Collections
    private TrafficManagerProfiles profiles;

    /**
     * Get a Configurable instance that can be used to create {@link TrafficManager}
     * with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new TrafficManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the TrafficManager
     */
    public static TrafficManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new TrafficManager(credentials.getEnvironment().newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the TrafficManager
     */
    public static TrafficManager authenticate(RestClient restClient, String subscriptionId) {
        return new TrafficManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing traffic manager management API entry points that work across subscriptions
         */
        TrafficManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl
            extends AzureConfigurableImpl<Configurable>
            implements Configurable {

        public TrafficManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return TrafficManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private TrafficManager(RestClient restClient, String subscriptionId) {
        super(restClient,
                subscriptionId,
                new TrafficManagerManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

    /**
     * @return entry point to traffic manager profile management
     */
    public TrafficManagerProfiles profiles() {
        if (this.profiles == null) {
            this.profiles = new TrafficManagerProfilesImpl(
                    super.innerManagementClient,
                    this);
        }
        return this.profiles;
    }
}
