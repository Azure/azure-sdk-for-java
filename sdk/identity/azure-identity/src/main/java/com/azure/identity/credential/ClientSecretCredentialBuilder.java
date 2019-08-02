// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link ClientSecretCredential}.
 *
 * @see ClientSecretCredential
 */
public class ClientSecretCredentialBuilder extends AadCredentialBuilderBase<ClientSecretCredentialBuilder> {
    private String tenantId;
    private String clientSecret;

    /**
     * Sets the tenant ID of the application.
     * @param tenantId the tenant ID of the application.
     * @return the ClientSecretCredentialBuilder itself
     */
    public ClientSecretCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return the ClientSecretCredentialBuilder itself
     */
    public ClientSecretCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * @return a {@link ClientSecretCredentialBuilder} with the current configurations.
     */
    public ClientSecretCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("tenantId", tenantId);
                put("clientSecret", clientSecret);
            }});
        return new ClientSecretCredential(tenantId, clientId, clientSecret, identityClientOptions);
    }
}
