// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation;

/**
 * Fluent client builder for instantiating an {@link ConfidentialClient}.
 *
 * @see ConfidentialClient
 */
public final class ConfidentialClientBuilder {
    private IdentityClientOptions identityClientOptions = new IdentityClientOptions();
    private String tenantId;
    private String clientId;
    private String clientSecret;


    /**
     * Sets the tenant ID for the client.
     * @param tenantId the tenant ID for the client.
     * @return the IdentityClientBuilder itself
     */
    public ConfidentialClientBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the client ID for the client.
     * @param clientId the client ID for the client.
     * @return the IdentityClientBuilder itself
     */
    public ConfidentialClientBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }


    /**
     * Sets the client secret for the client.
     * @param clientSecret the secret value of the Microsoft Entra application.
     * @return the IdentityClientBuilder itself
     */
    public ConfidentialClientBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }


    /**
     * Sets the options for the client.
     * @param identityClientOptions the options for the client.
     * @return the IdentityClientBuilder itself
     */
    public ConfidentialClientBuilder identityClientOptions(IdentityClientOptions identityClientOptions) {
        this.identityClientOptions = identityClientOptions;
        return this;
    }


    /**
     * @return a {@link ConfidentialClient} with the current configurations.
     */
    public ConfidentialClient build() {
        return new ConfidentialClient(tenantId, clientId, clientSecret, identityClientOptions);
    }
}
