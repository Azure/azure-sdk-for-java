// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import java.util.List;

import com.azure.communication.administration.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.administration.implementation.CommunicationIdentityImpl;
import com.azure.communication.administration.models.CommunicationIdentityAccessToken;
import com.azure.communication.administration.models.CommunicationIdentityTokenScope;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

/**
 * Synchronous client interface for Communication Service identity operations
 */
@ServiceClient(builder = CommunicationIdentityClientBuilder.class, isAsync = false)
public final class CommunicationIdentityClient {

    private final CommunicationIdentityImpl client;
    private final ClientLogger logger = new ClientLogger(CommunicationIdentityClient.class);

    CommunicationIdentityClient(CommunicationIdentityClientImpl communicationIdentityClient) {
        client = communicationIdentityClient.getCommunicationIdentity();
    }

     /**
     * Creates a new CommunicationUserIdentifier.
     *
     * @param scopes the list of scopes for the identity access token
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationUserIdentifier createUser(List<CommunicationIdentityTokenScope> scopes) {
        return null;
    }

    /**
     * Creates a new CommunicationUserIdentifier with response.
     *
     * @param scopes the list of scopes for the identity access token
     * @param context A {@link Context} representing the request context.
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationUserIdentifier> createUserWithResponse(List<CommunicationIdentityTokenScope> scopes, Context context) {
        return null;
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its data.
     *
     * @param communicationUser The user to be deleted.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteUser(CommunicationUserIdentifier communicationUser) {
        return;
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its data with response.
     *
     * @param communicationUser The user to be deleted.
     * @param context A {@link Context} representing the request context.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteUserWithResponse(CommunicationUserIdentifier communicationUser, Context context) {
        return null;
    }

      /**
     * Revokes all the access tokens created for an identifier.
     * 
     * @param communicationUser The user to be revoked access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void revokeAccessTokens(CommunicationUserIdentifier communicationUser) {
        return;
    }


    /**
     * Revokes all the tokens created for a user before a specific date.
     *
     * @param communicationUser The user to be revoked access token.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public Response<Void> revokeAccessTokensWithResponse(CommunicationUserIdentifier communicationUser, Context context) {
        return null;
    }

    /**
     * Generates a new access token for an identity.
     *
     * @param communicationUser The user to be issued access tokens.
     * @param scopes The scopes that the token should have.
     * @return the issued access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationIdentityAccessToken issueAccessToken(CommunicationUserIdentifier communicationUser, List<CommunicationIdentityTokenScope> scopes) {
        return null;
    }

    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The CommunicationUser from whom to issue a token.
     * @param scopes The scopes that the token should have.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the created CommunicationUserToken.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)    
    public Response<CommunicationIdentityAccessToken> issueAccessTokenWithResponse(
        CommunicationUserIdentifier communicationUser, List<CommunicationIdentityTokenScope> scopes, Context context) {
        return null;
    }
}
