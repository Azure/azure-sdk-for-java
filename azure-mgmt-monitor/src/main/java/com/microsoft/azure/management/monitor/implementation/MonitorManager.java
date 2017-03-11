/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.monitor.AutoscaleSettings;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure monitor resource management.
 */
public final class MonitorManager extends Manager<MonitorManager, MonitorManagementClientImpl> {
    // Collections
    private AutoscaleSettings autoscaleSettings;

    private MonitorManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new MonitorManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

    /**
     * Get a Configurable instance that can be used to create MonitorManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new MonitorManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of MonitorManager that exposes Monitor resource management API entry points.
     *
     * @param credentials    the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the MonitorManager
     */
    public static MonitorManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new MonitorManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of MonitorManager that exposes monitor resource management API entry points.
     *
     * @param restClient     the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the MonitorManager
     */
    public static MonitorManager authenticate(RestClient restClient, String subscriptionId) {
        return new MonitorManager(restClient, subscriptionId);
    }

    /**
     * @return the Monitor management API entry point
     */
    public AutoscaleSettings autoscaleSettings() {
        if (autoscaleSettings == null) {
            //autoscaleSettings = new RedisCachesImpl(this);
        }
        return autoscaleSettings;
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of MonitorManager that exposes Monitor management API entry points.
         *
         * @param credentials    the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing Monitor management API entry points that work across subscriptions
         */
        MonitorManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public MonitorManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return MonitorManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }
}
