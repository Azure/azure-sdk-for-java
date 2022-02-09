// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for Azure Arc Service.
 */
@Immutable
class ArcIdentityCredential extends ManagedIdentityServiceCredential {
    private final String identityEndpoint;
    private final ClientLogger logger = new ClientLogger(ArcIdentityCredential.class);

    /**
     * Creates an instance of {@link ArcIdentityCredential}.
     *
     * @param clientId The client ID of user assigned or system assigned identity.
     * @param identityClient The identity client to acquire a token with.
     */
    ArcIdentityCredential(String clientId, IdentityClient identityClient) {
        super(clientId, identityClient, "AZURE ARC IDENTITY ENDPOINT");
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        this.identityEndpoint = configuration.get(Configuration.PROPERTY_IDENTITY_ENDPOINT);
        if (identityEndpoint != null) {
            validateEndpointProtocol(this.identityEndpoint, "Identity", logger);
        }
    }

    /**
     * Gets an access token for a token request.
     *
     * @param request The details of the token request.
     * @return A publisher that emits an {@link AccessToken}.
     */
    public Mono<AccessToken> authenticate(TokenRequestContext request) {
        if  (getClientId() != null) {
            return Mono.error(logger.logExceptionAsError(new ClientAuthenticationException(
                "User assigned identity is not supported by the Azure Arc Managed Identity Endpoint. To authenticate "
                    + "with the system assigned identity omit the client id when constructing the"
                    + " ManagedIdentityCredential.", null)));
        }
        return identityClient.authenticateToArcManagedIdentityEndpoint(identityEndpoint, request);
    }
}
