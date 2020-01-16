// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link ClientSecretCredential}.
 *
 * @see ClientSecretCredential
 */
public class ClientSecretCredentialBuilder extends AadCredentialBuilderBase<ClientSecretCredentialBuilder> {
    private String clientSecret;

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
     * Creates a new {@link ClientCertificateCredential} with the current configurations.
     *
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
