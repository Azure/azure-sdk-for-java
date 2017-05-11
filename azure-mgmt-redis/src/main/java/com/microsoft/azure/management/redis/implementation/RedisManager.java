/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.redis.RedisCaches;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure redis resource management.
 */
public final class RedisManager extends Manager<RedisManager, RedisManagementClientImpl> {
    // Collections
    private RedisCaches redisCaches;

    private RedisManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new RedisManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

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
     * @param credentials    the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the RedisManager
     */
    public static RedisManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new RedisManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of RedisManager that exposes redis resource management API entry points.
     *
     * @param restClient     the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the RedisManager
     */
    public static RedisManager authenticate(RestClient restClient, String subscriptionId) {
        return new RedisManager(restClient, subscriptionId);
    }

    /**
     * @return the Redis Cache management API entry point
     */
    public RedisCaches redisCaches() {
        if (redisCaches == null) {
            redisCaches = new RedisCachesImpl(this);
        }
        return redisCaches;
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of RedisManager that exposes Redis management API entry points.
         *
         * @param credentials    the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing Redis management API entry points that work across subscriptions
         */
        RedisManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public RedisManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return RedisManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }
}
