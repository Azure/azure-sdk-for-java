// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

/**
 * Represents Managed Identity Client Options used in Managed Identity OAuth Flow .
 */
public class ManagedIdentityClientOptions extends ClientOptions {
    private String resourceId;
    private String objectId;
    private boolean useImdsRetryStrategy;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public ManagedIdentityClientOptions() {
        super();
    }

    /**
     * Gets the configured resource ID.
     * @return the client secret
     */
    public String getResourceId() {
        return this.resourceId;
    }

    /**
     * Sets the resource ID.
     * @param resourceId The resource ID
     * @return the ConfidentialClientOptions itself.
     */
    public ManagedIdentityClientOptions setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Gets the configured object ID.
     * @return the client secret
     */
    public String getObjectId() {
        return this.objectId;
    }

    /**
     * Sets the object ID.
     * @param objectId The object ID
     * @return the ConfidentialClientOptions itself.
     */
    public ManagedIdentityClientOptions setObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    @Override
    public ManagedIdentityClientOptions clone() {
        ManagedIdentityClientOptions clone
            = (ManagedIdentityClientOptions) new ManagedIdentityClientOptions().setResourceId(resourceId)
                .setObjectId(objectId)
                .setClientId(this.getClientId())
                .setTenantId(this.getTenantId())
                .setHttpPipelineOptions(this.getHttpPipelineOptions().clone())
                .setExecutorService(this.getExecutorService())
                .setAuthorityHost(this.getAuthorityHost())
                .setAdditionallyAllowedTenants(this.getAdditionallyAllowedTenants())
                .setTokenCacheOptions(this.getTokenCacheOptions());
        return clone;
    }

    /**
     * Sets the boolean flag indicating whether IMDS retry strategy should be used or not.
     *
     * @param useImdsRetryStrategy the boolean flag indicating to use IMDS retry strategy
     * @return the updated options
     */
    public ManagedIdentityClientOptions setUseImdsRetryStrategy(boolean useImdsRetryStrategy) {
        this.useImdsRetryStrategy = useImdsRetryStrategy;
        return this;
    }

    /**
     * Gets the retry strategy for the IMDS scenario.
     *
     * @return the imds retry strategy.
     */
    public boolean getUseImdsRetryStrategy() {
        return this.useImdsRetryStrategy;
    }
}
