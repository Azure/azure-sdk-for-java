// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

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

    public Duration getProcessTimeout() {
        return processTimeout;
    }

    public DevToolsClientOptions setProcessTimeout(Duration processTimeout) {
        this.processTimeout = processTimeout;
        return this;
    }

    public String getSubscription() {
        return subscription;
    }

    public void setSubscription(String subscription) {
        this.subscription = subscription;
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
