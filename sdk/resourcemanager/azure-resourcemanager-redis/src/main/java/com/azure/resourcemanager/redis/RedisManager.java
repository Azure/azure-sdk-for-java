// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.redis.implementation.RedisCachesImpl;
import com.azure.resourcemanager.redis.models.RedisCaches;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.Manager;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;

/** Entry point to Azure redis resource management. */
public final class RedisManager extends Manager<RedisManager, RedisManagementClient> {
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
    public static RedisManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return authenticate(httpPipeline, profile, new SdkContext());
    }

    /**
     * Creates an instance of RedisManager that exposes Redis resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the RedisManager
     */
    public static RedisManager authenticate(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new RedisManager(httpPipeline, profile, sdkContext);
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

    private RedisManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new RedisManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
            sdkContext);
    }

    /** @return the Redis Cache management API entry point */
    public RedisCaches redisCaches() {
        if (redisCaches == null) {
            redisCaches = new RedisCachesImpl(this);
        }
        return redisCaches;
    }
}
