// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import com.azure.communication.administration.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.administration.implementation.CommunicationIdentityImpl;
import com.azure.communication.administration.models.CommunicationIdentityAccessToken;
import com.azure.communication.administration.models.CommunicationIdentityTokenScope;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;

import java.util.List;
import reactor.core.publisher.Mono;

/**
 * Asynchronous client interface for Azure Communication Services identity operations
 */
@ServiceClient(builder = CommunicationIdentityClientBuilder.class, isAsync = true)
public final class CommunicationIdentityAsyncClient {

    private final CommunicationIdentityImpl client;
    private final ClientLogger logger = new ClientLogger(CommunicationIdentityAsyncClient.class);

    CommunicationIdentityAsyncClient(CommunicationIdentityClientImpl communicationIdentityServiceClient) {
        client = communicationIdentityServiceClient.getCommunicationIdentity();
    }

    /**
     * Creates a new CommunicationUserIdentifier.
     *
     * @param scopes the list of scopes for the identity access token
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationUserIdentifier> createUser(List<CommunicationIdentityTokenScope> scopes) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Creates a new CommunicationUserIdentifier with response.
     *
     * @param scopes the list of scopes for the identity access token
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationUserIdentifier>> createUserWithResponse(List<CommunicationIdentityTokenScope> scopes) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its data.
     *
     * @param communicationUser the user to be deleted.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteUser(CommunicationUserIdentifier communicationUser) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its data with response.
     * @return the response.
     *
     * @param communicationUser The user to be deleted.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteUserWithResponse(CommunicationUserIdentifier communicationUser) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));  
    }

    /**
     * Revokes all the access tokens created for an identifier.
     * 
     * @param communicationUser The user to be revoked access tokens.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> revokeAccessTokens(CommunicationUserIdentifier communicationUser) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));  
    }

    /**
     * Revokes all the access tokens created for an identifier with response.
     *
     * @param communicationUser The user to be revoked access tokens.
     * @return the response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> revokeAccessTokensWithResponse(CommunicationUserIdentifier communicationUser) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Generates a new access token for an identity.
     *
     * @param communicationUser The user to be issued access tokens.
     * @param scopes The scopes that the token should have.
     * @return the issued access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationIdentityAccessToken> issueAccessToken(CommunicationUserIdentifier communicationUser, List<CommunicationIdentityTokenScope> scopes) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }

    /**
     * Generates a new access token for an identity with response.
     *
     * @param communicationUser The user to be issued access tokens.
     * @param scopes The scopes that the token should have.
     * @return the issued access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationIdentityAccessToken>> issueAccessTokenWithResponse(CommunicationUserIdentifier communicationUser, List<CommunicationIdentityTokenScope> scopes) {
        return Mono.error(new UnsupportedOperationException("not yet implemented"));
    }
}
