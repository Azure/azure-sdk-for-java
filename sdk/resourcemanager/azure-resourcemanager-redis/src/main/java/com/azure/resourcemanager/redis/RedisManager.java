// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.redis.fluent.RedisManagementClient;
import com.azure.resourcemanager.redis.implementation.RedisManagementClientBuilder;
import com.azure.resourcemanager.redis.implementation.RedisCachesImpl;
import com.azure.resourcemanager.redis.models.RedisCaches;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

/** Entry point to Azure redis resource management. */
public final class RedisManager extends Manager<RedisManagementClient> {
    // Collections
    private RedisCaches redisCaches;

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
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the RedisManager
     */
    public static RedisManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of RedisManager that exposes Redis resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the RedisManager
     */
    private static RedisManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new RedisManager(httpPipeline, profile);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of RedisManager that exposes Redis management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing Redis management API entry points that work across subscriptions
         */
        RedisManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {

        public RedisManager authenticate(TokenCredential credential, AzureProfile profile) {
            return RedisManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private RedisManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new RedisManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
    }

    /** @return the Redis Cache management API entry point */
    public RedisCaches redisCaches() {
        if (redisCaches == null) {
            redisCaches = new RedisCachesImpl(this);
        }
        return redisCaches;
    }
}
