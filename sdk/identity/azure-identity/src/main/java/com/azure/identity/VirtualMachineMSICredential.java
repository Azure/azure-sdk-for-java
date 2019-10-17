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
class VirtualMachineMSICredential {

    private final IdentityClient identityClient;
    private final String clientId;

    /**
     * Creates an instance of VirtualMachineMSICredential.
     * @param clientId the client id of user assigned or system assigned identity
     * @param identityClient the identity client to acquire a token with.
     */
    VirtualMachineMSICredential(String clientId, IdentityClient identityClient) {
        this.clientId = clientId;
        this.identityClient = identityClient;
    }

    /**
     * @return the client id of user assigned or system assigned identity.
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Gets the token for a list of scopes.
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticate(TokenRequestContext request) {
        return identityClient.authenticateToIMDSEndpoint(request);
    }
}
