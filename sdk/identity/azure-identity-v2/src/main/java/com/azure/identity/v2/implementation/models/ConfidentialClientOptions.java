// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

import io.clientcore.core.http.pipeline.HttpPipeline;

import java.util.function.Function;

/**
 * Represents Confidential Client Options used in Confidential Client OAuth Flow .
 */
public class ConfidentialClientOptions extends ClientOptions {
    private String clientSecret;
    private Function<HttpPipeline, String> clientAssertionFunction;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public ConfidentialClientOptions() {
        super();
    }

    /**
     * Gets the configured client secret.
     * @return the client secret
     */
    public String getClientSecret() {
        return this.clientSecret;
    }

    /**
     * Sets the client secret
     * @param clientSecret The client secret
     * @return the ConfidentialClientOptions itself.
     */
    public ConfidentialClientOptions setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public ConfidentialClientOptions
        setClientAssertionFunction(Function<HttpPipeline, String> clientAssertionFunction) {
        this.clientAssertionFunction = clientAssertionFunction;
        return this;
    }

    public Function<HttpPipeline, String> getClientAssertionFunction() {
        return this.clientAssertionFunction;
    }

    @Override
    public ConfidentialClientOptions clone() {
        ConfidentialClientOptions clone
            = (ConfidentialClientOptions) new ConfidentialClientOptions().setClientSecret(this.clientSecret)
                .setClientAssertionFunction(this.clientAssertionFunction)
                .setClientId(this.getClientId())
                .setTenantId(this.getTenantId())
                .setHttpPipelineOptions(this.getHttpPipelineOptions().copy())
                .setExecutorService(this.getExecutorService())
                .setAuthorityHost(this.getAuthorityHost())
                .setAdditionallyAllowedTenants(this.getAdditionallyAllowedTenants())
                .setTokenCacheOptions(this.getTokenCacheOptions());
        return clone;
    }
}
