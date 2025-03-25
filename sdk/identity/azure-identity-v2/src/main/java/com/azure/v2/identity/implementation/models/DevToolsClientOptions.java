// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

import java.time.Duration;

/**
 * Options to configure the IdentityClient.
 */
public class DevToolsClientOptions extends ClientOptions {
    private Duration processTimeout;
    private String subscription;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public DevToolsClientOptions() {
        super();
    }

    /**
     * Gets the configured process timeout.
     *
     * @return the process timeout.
     */
    public Duration getProcessTimeout() {
        return processTimeout;
    }

    /**
     * Sets the process timeout.
     *
     * @param processTimeout the process timeout.
     * @return the updated options
     */
    public DevToolsClientOptions setProcessTimeout(Duration processTimeout) {
        this.processTimeout = processTimeout;
        return this;
    }

    /**
     * Gets the configured subscription name/ID.
     *
     * @return the subscription name/ID
     */
    public String getSubscription() {
        return subscription;
    }

    /**
     * Sets the subscription name/ID.
     *
     * @param subscription the subscription name/ID
     * @return the updated options
     */
    public DevToolsClientOptions setSubscription(String subscription) {
        this.subscription = subscription;
        return this;
    }

    @Override
    public DevToolsClientOptions clone() {
        DevToolsClientOptions clone
            = (DevToolsClientOptions) new DevToolsClientOptions().setProcessTimeout(processTimeout)
                .setClientId(this.getClientId())
                .setTenantId(this.getTenantId())
                .setHttpPipelineOptions(this.getHttpPipelineOptions().clone())
                .setExecutorService(this.getExecutorService())
                .setAuthorityHost(this.getAuthorityHost())
                .setAdditionallyAllowedTenants(this.getAdditionallyAllowedTenants())
                .setTokenCacheOptions(this.getTokenCacheOptions());
        return clone;
    }
}
