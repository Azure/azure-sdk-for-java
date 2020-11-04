// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.implementation.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for Virtual Machines.
 */
@Immutable
class VirtualMachineMsiCredential extends ManagedIdentityServiceCredential {

    /**
     * Creates an instance of VirtualMachineMSICredential.
     * @param clientId the client id of user assigned or system assigned identity
     * @param identityClient the identity client to acquire a token with.
     */
    VirtualMachineMsiCredential(String clientId, IdentityClient identityClient) {
        super(clientId, identityClient, "AZURE VM IMDS ENDPOINT");
    }

    /**
     * Gets an access token for the token request.
     *
     * @param request The details of the token request.
     * @return A publisher that emits an {@link AccessToken}.
     */
    public Mono<AccessToken> authenticate(TokenRequestContext request) {
        return identityClient.authenticateToIMDSEndpoint(request);
    }
}
