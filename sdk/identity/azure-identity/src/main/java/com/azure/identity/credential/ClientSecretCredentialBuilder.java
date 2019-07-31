// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link ClientSecretCredentialBuilder}.
 *
 * @see ClientSecretCredentialBuilder
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
     * @return a {@link ClientSecretCredentialBuilder} with the current configurations.
     */
    public ClientSecretCredential build() {
        List<String> missing = new ArrayList<>();
        if (clientId == null) {
            missing.add("clientId");
        }
        if (tenantId == null) {
            missing.add("tenantId");
        }
        if (clientSecret == null) {
            missing.add("clientSecret");
        }
        if (missing.size() > 0) {
            throw new IllegalArgumentException("Must provide non-null values for "
                + String.join(", ", missing) + " properties in " + this.getClass().getSimpleName());
        }
        return new ClientSecretCredential(tenantId, clientId, clientSecret, identityClientOptions);
    }
}
