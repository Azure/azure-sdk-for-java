// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * An AAD credential that acquires a token with a client secret for an AAD application.
 */
@Immutable
public class ClientSecretCredential implements TokenCredential {
    /* The client secret value. */
    private final String clientSecret;
    private final IdentityClient identityClient;

    /**
     * Creates a ClientSecretCredential with the given identity client options.
     *
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param clientSecret the secret value of the AAD application.
     * @param identityClientOptions the options for configuring the identity client
     */
    ClientSecretCredential(String tenantId, String clientId, String clientSecret, IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(clientSecret);
        Objects.requireNonNull(identityClientOptions);
        identityClient = new IdentityClient(tenantId, clientId, identityClientOptions);
        this.clientSecret = clientSecret;
    }

    @Override
    public Mono<AccessToken> getToken(String... scopes) {
        return identityClient.authenticateWithClientSecret(clientSecret, scopes);
    }
}
