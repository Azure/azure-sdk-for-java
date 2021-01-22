// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential.
 */
abstract class ManagedIdentityServiceCredential {
    private final String clientId;
    private final String environment;
    final IdentityClient identityClient;

    /**
     * Creates an instance of ManagedIdentityServiceCredential.
     * @param clientId the client id of user assigned or system assigned identity
     * @param identityClient the identity client to acquire a token with.
     * @param environment The service environment of the credential.
     */
    ManagedIdentityServiceCredential(String clientId, IdentityClient identityClient, String environment) {
        this.identityClient = identityClient;
        this.clientId = clientId;
        this.environment = environment;
    }

    /**
     * Gets an access token for a token request.
     *
     * @param request The details of the token request.
     * @return A publisher that emits an {@link AccessToken}.
     */
    public abstract Mono<AccessToken> authenticate(TokenRequestContext request);

    /**
     * @return the client ID of user assigned or system assigned identity.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the environment of the Maanged Identity.
     */
    public String getEnvironment() {
        return environment;
    }

    void validateEndpointProtocol(String endpoint, String endpointName, ClientLogger logger) {
        if (!(endpoint.startsWith("https") || endpoint.startsWith("http"))) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException(
                    String.format("%s endpoint should start with 'https' or 'http' scheme.", endpointName)));
        }
    }
}
