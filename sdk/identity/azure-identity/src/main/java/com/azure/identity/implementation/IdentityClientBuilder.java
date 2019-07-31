// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

/**
 * Fluent client builder for instantiating an {@link IdentityClient}.
 *
 * @see IdentityClient
 */
public final class IdentityClientBuilder {
    private IdentityClientOptions identityClientOptions;
    private String tenantId;
    private String clientId;

    /**
     * Sets the tenant ID for the client.
     * @param tenantId the tenant ID for the client.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the client ID for the client.
     * @param clientId the client ID for the client.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the options for the client.
     * @param identityClientOptions the options for the client.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder identityClientOptions(IdentityClientOptions identityClientOptions) {
        this.identityClientOptions = identityClientOptions;
        return this;
    }

    /**
     * @return a {@link IdentityClient} with the current configurations.
     */
    public IdentityClient build() {
        return new IdentityClient(tenantId, clientId, identityClientOptions);
    }
}
