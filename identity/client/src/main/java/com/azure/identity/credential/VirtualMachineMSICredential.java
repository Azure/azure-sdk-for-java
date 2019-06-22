// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.identity.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for Virtual Machines.
 */
class VirtualMachineMSICredential {

    private final IdentityClient identityClient;
    private String clientId;

    /**
     * Creates an instance of VirtualMachineMSICredential.
     * @param identityClient the identity client to acquire a token with.
     */
    public VirtualMachineMSICredential(IdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    /**
     * @return the client id of user assigned or system assigned identity.
     */
    public String clientId() {
        return this.clientId;
    }

    /**
     * Specifies the client id of user assigned or system assigned identity.
     *
     * @param clientId the client id
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Gets the token for a list of scopes.
     * @param scopes the scopes to get token for
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticate(String[] scopes) {
        return identityClient.authenticateToIMDSEndpoint(clientId(), scopes);
    }
}
