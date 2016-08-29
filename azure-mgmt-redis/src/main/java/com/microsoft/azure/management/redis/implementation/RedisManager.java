/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Entry point to Azure redis resource management.
 */
public final class RedisManager extends Manager<RedisManager, RedisManagementClientImpl> {
    // Collections
    /**
     * Get a Configurable instance that can be used to create RedisManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new RedisManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of RedisManager that exposes Redis resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the RedisManager
     */
    public static RedisManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new RedisManager(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of RedisManager that exposes redis resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the RedisManager
     */
    public static RedisManager authenticate(RestClient restClient, String subscriptionId) {
        return new RedisManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of RedisManager that exposes Redis management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing Redis management API entry points that work across subscriptions
         */
        RedisManager authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public RedisManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
            return RedisManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private RedisManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new RedisManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
        }
}
