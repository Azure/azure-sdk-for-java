// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.AccessToken;
import com.azure.identity.IdentityClient;
import com.azure.identity.IdentityClientOptions;
import reactor.core.publisher.Mono;

/**
 * An AAD credential that acquires a token with a client secret for an AAD application.
 */
public class ClientSecretCredential extends AadCredential<ClientSecretCredential> {
    /* The client secret value. */
    private String clientSecret;
    private final IdentityClient identityClient;

    /**
     * Creates a ClientSecretCredential with default identity client options.
     */
    public ClientSecretCredential() {
        this(new IdentityClientOptions());
    }

    /**
     * Creates a ClientSecretCredential with the given identity client options.
     *
     * @param identityClientOptions the options for configuring the identity client
     */
    public ClientSecretCredential(IdentityClientOptions identityClientOptions) {
        identityClient = new IdentityClient(identityClientOptions);
    }

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return the credential itself
     */
    public ClientSecretCredential clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @Override
    public Mono<String> getToken(String... scopes) {
        validate();
        if (clientSecret == null) {
            return Mono.error(new IllegalArgumentException("Non-null value must be provided for clientSecret property in ClientSecretCredential"));
        }
        return identityClient.authenticateWithClientSecret(tenantId(), clientId(), clientSecret, scopes).map(AccessToken::token);
    }
}
