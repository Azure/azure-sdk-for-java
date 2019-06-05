// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.identity.AccessToken;
import com.azure.identity.IdentityClient;
import reactor.core.publisher.Mono;

/**
 * The Managed Service Identity credential for Virtual Machines.
 */
public final class VirtualMachineMSICredential {

    private final IdentityClient identityClient;

    private String objectId;
    private String clientId;
    private String identityId;

    /**
     * Creates an instance of VirtualMachineMSICredential.
     * @param identityClient the identity client to acquire a token with.
     */
    public VirtualMachineMSICredential(IdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    /**
     * @return the principal id of user assigned or system assigned identity.
     */
    public String objectId() {
        return this.objectId;
    }

    /**
     * @return the client id of user assigned or system assigned identity.
     */
    public String clientId() {
        return this.clientId;
    }

    /**
     * @return the ARM resource id of the user assigned identity resource.
     */
    public String identityId() {
        return this.identityId;
    }

    /**
     * specifies the principal id of user assigned or system assigned identity.
     *
     * @param objectId the object (principal) id
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential objectId(String objectId) {
        this.objectId = objectId;
        return this;
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
     * Specifies the ARM resource id of the user assigned identity resource.
     *
     * @param identityId the identity ARM id
     * @return VirtualMachineMSICredential
     */
    public VirtualMachineMSICredential identityId(String identityId) {
        this.identityId = identityId;
        return this;
    }

    /**
     * Gets the token for a list of scopes.
     * @param scopes the scopes to get token for
     * @return a Publisher that emits an AccessToken
     */
    public Mono<AccessToken> authenticate(String[] scopes) {
        return identityClient.managedIdentityClient().authenticateToIMDSEndpoint(clientId(), objectId(), identityId(), scopes);
    }
}
