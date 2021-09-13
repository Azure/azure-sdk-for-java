// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;

import reactor.core.publisher.Mono;

/**
 * Authenticates a service principal with AAD using a client assertion.
 */
class ClientAssertionCredential extends ManagedIdentityServiceCredential {
    private final ClientLogger logger = new ClientLogger(ManagedIdentityCredential.class);

    /**
     * Creates an instance of ClientAssertionCredential.
     *
     * @param clientId the client id of user assigned or system assigned identity.
     * @param identityClient the identity client to acquire a token with.
     */
    ClientAssertionCredential(String clientId, IdentityClient identityClient) {
        super(clientId, identityClient, "AZURE AKS TOKEN EXCHANGE");
    }

    @Override
    public Mono<AccessToken> authenticate(TokenRequestContext request) {
        if (this.getClientId() == null) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException("The client id is not configured via"
                + " 'AZURE_CLIENT_ID' environment variable or through the credential builder."
                + " Please ensure client id is provided to authenticate via token exchange in AKS environment.")));
        }
        return identityClient.authenticatewithExchangeToken(request);
    }
}
