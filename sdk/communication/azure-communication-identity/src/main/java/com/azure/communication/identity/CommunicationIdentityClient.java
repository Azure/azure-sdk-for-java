// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import java.util.List;
import java.util.Objects;

import com.azure.communication.identity.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.identity.implementation.CommunicationIdentityImpl;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessToken;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenResult;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierWithTokenResult;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AccessToken;
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
        client = communicationIdentityClient.getCommunicationIdentity();
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
        context = context == null ? Context.NONE : context;
        Response<CommunicationIdentityAccessTokenResult> response = 
            client.createWithResponseAsync(new CommunicationIdentityCreateRequest(), context).block();
        
        if (response != null && response.getValue() != null) {
            String id = response.getValue().getIdentity().getId();
            return new SimpleResponse<CommunicationUserIdentifier>(
                response,
                new CommunicationUserIdentifier(id));
        }
        return null;
    }

    /**
     * Creates a new CommunicationUserIdentifier with token.
     *
     * @param scopes the list of scopes for the token
     * @return the result with created communication user and token
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationUserIdentifierWithTokenResult createUserWithToken(
        List<CommunicationTokenScope> scopes) {
        Objects.requireNonNull(scopes);
        CommunicationIdentityAccessTokenResult result = client.create(
            new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopes));
        CommunicationUserIdentifier user = 
            new CommunicationUserIdentifier(result.getIdentity().getId());
        AccessToken token = new AccessToken(result.getAccessToken().getToken(), result.getAccessToken().getExpiresOn());
        return new CommunicationUserIdentifierWithTokenResult(user, token);
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
        List<CommunicationTokenScope> scopes, Context context) {
        Objects.requireNonNull(scopes);
        context = context == null ? Context.NONE : context;

        Response<CommunicationIdentityAccessTokenResult> response = 
            client.createWithResponseAsync(
                new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopes), context).block();

        if (response != null && response.getValue() != null) {
            CommunicationUserIdentifier user = new CommunicationUserIdentifier(response.getValue().getIdentity().getId());
            AccessToken token = new AccessToken(
                response.getValue().getAccessToken().getToken(),
                response.getValue().getAccessToken().getExpiresOn());
            return new SimpleResponse<CommunicationUserIdentifierWithTokenResult>(
                response,
                new CommunicationUserIdentifierWithTokenResult(user, token));
        }
        return null;
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data.
     *
     * @param communicationUser The user to be deleted.
     * @return the response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void deleteUser(CommunicationUserIdentifier communicationUser) {
        Objects.requireNonNull(communicationUser);
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
        Objects.requireNonNull(communicationUser);
        context = context == null ? Context.NONE : context;
        return client.deleteWithResponseAsync(communicationUser.getId(), context).block();
    }

    /**
     * Revokes all the tokens created for an identifier.
     * 
     * @param communicationUser The user to be revoked token.
     * @return the response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void revokeTokens(CommunicationUserIdentifier communicationUser) {
        Objects.requireNonNull(communicationUser);
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
        Objects.requireNonNull(communicationUser);
        context = context == null ? Context.NONE : context;
        return client.revokeAccessTokensWithResponseAsync(communicationUser.getId(), context).block();
    }

    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The user to be issued tokens.
     * @param scopes The scopes that the token should have.
     * @return the issued token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessToken issueToken(CommunicationUserIdentifier communicationUser,
        List<CommunicationTokenScope> scopes) {
        Objects.requireNonNull(communicationUser);
        Objects.requireNonNull(scopes);
        CommunicationIdentityAccessToken rawToken = client.issueAccessToken(
            communicationUser.getId(),
            new CommunicationIdentityAccessTokenRequest().setScopes(scopes));
        return new AccessToken(rawToken.getToken(), rawToken.getExpiresOn());
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
    public Response<AccessToken> issueTokenWithResponse(CommunicationUserIdentifier communicationUser,
        List<CommunicationTokenScope> scopes, Context context) {
        Objects.requireNonNull(communicationUser);
        Objects.requireNonNull(scopes);
        context = context == null ? Context.NONE : context;
        Response<CommunicationIdentityAccessToken> response = client.issueAccessTokenWithResponseAsync(
            communicationUser.getId(),
            new CommunicationIdentityAccessTokenRequest().setScopes(scopes),
            context)
            .block();
        if (response != null && response.getValue() != null) {
            return new SimpleResponse<AccessToken>(
                response,
                new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn()));
        }
        return null;
    }
}
