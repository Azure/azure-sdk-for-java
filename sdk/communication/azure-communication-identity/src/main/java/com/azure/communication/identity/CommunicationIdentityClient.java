// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import java.util.List;

import com.azure.communication.identity.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.identity.implementation.CommunicationIdentityImpl;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenResult;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationIdentityTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierWithTokenResult;
import com.azure.communication.identity.models.CommunicationUserToken;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
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
        client = communicationIdentityClient.getCommunicationIdentities();
    }

    /**
     * Creates a new CommunicationUserIdentifier.
     *
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationUserIdentifier createUser() {
        CommunicationIdentityAccessTokenResult result = client.create(new CommunicationIdentityCreateRequest());
        return new CommunicationUserIdentifier(result.getIdentity().getId());
    }

    /**
     * Creates a new CommunicationUserIdentifier with response.
     *
     * @param context A {@link Context} representing the request context.
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationUserIdentifier> createUserWithResponse(Context context) {
        Response<CommunicationIdentityAccessTokenResult> response = 
            client.createWithResponse(new CommunicationIdentityCreateRequest(), context);
        String id = response.getValue().getIdentity().getId();
        
        return new SimpleResponse<CommunicationUserIdentifier>(
            response,
            new CommunicationUserIdentifier(id));
    }

    /**
     * Creates a new CommunicationUserIdentifier with token.
     *
     * @param scopes the list of scopes for the token
     * @return the result with created communication user and token
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationUserIdentifierWithTokenResult createUserWithToken(
        List<CommunicationIdentityTokenScope> scopes) {
        CommunicationIdentityAccessTokenResult result = client.create(
            new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopes));
        CommunicationUserIdentifier user = 
            new CommunicationUserIdentifier(result.getIdentity().getId());
        
        return new CommunicationUserIdentifierWithTokenResult(user, result.getAccessToken());
    }

    /**
     * Creates a new CommunicationUserIdentifier with token with response.
     *
     * @param scopes the list of scopes for the token
     * @param context A {@link Context} representing the request context.
     * @return the result with created communication user and token
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationUserIdentifierWithTokenResult> createUserWithTokenWithResponse(
        List<CommunicationIdentityTokenScope> scopes, Context context) {
        Response<CommunicationIdentityAccessTokenResult> response = 
            client.createWithResponse(new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopes), context);
        String id = response.getValue().getIdentity().getId();
    
        return new SimpleResponse<CommunicationUserIdentifierWithTokenResult>(
            response,
            new CommunicationUserIdentifierWithTokenResult(
                new CommunicationUserIdentifier(id),
                response.getValue().getAccessToken()));
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data.
     *
     * @param communicationUser The user to be deleted.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void deleteUser(CommunicationUserIdentifier communicationUser) {
        return client.deleteAsync(communicationUser.getId()).block();
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data with response.
     *
     * @param communicationUser The user to be deleted.
     * @param context A {@link Context} representing the request context.
     * @return the response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteUserWithResponse(CommunicationUserIdentifier communicationUser, Context context) {
        return client.deleteWithResponse(communicationUser.getId(), context);
    }

    /**
     * Revokes all the tokens created for an identifier.
     * 
     * @param communicationUser The user to be revoked token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void revokeTokens(CommunicationUserIdentifier communicationUser) {
        return client.revokeAccessTokensAsync(communicationUser.getId()).block();
    }

    /**
     * Revokes all the tokens created for a user before a specific date.
     *
     * @param communicationUser The user to be revoked token.
     * @param context the context of the request. Can also be null or
     *                          Context.NONE.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> revokeTokensWithResponse(CommunicationUserIdentifier communicationUser, Context context) {
        return client.revokeAccessTokensWithResponse(communicationUser.getId(), context);
    }

    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The user to be issued tokens.
     * @param scopes The scopes that the token should have.
     * @return the issued token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationUserToken issueToken(CommunicationUserIdentifier communicationUser,
        List<CommunicationIdentityTokenScope> scopes) {
        return client.issueAccessToken(
            communicationUser.getId(),
            new CommunicationIdentityAccessTokenRequest().setScopes(scopes));
    }

    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The CommunicationUser from whom to issue a token.
     * @param scopes The scopes that the token should have.
     * @param context the context of the request. Can also be null or
     *                          Context.NONE.
     * @return the created CommunicationUserToken.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationUserToken> issueTokenWithResponse(CommunicationUserIdentifier communicationUser,
            List<CommunicationIdentityTokenScope> scopes, Context context) {
          return client.issueAccessTokenWithResponse(
            communicationUser.getId(),
            new CommunicationIdentityAccessTokenRequest().setScopes(scopes),
            context);
    }
}
