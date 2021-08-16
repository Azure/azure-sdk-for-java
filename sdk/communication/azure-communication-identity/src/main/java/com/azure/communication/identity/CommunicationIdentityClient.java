// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.azure.communication.identity.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.identity.implementation.CommunicationIdentityImpl;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessToken;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenResult;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
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
     * @return The created Communication User.
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
     * @return The created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationUserIdentifier> createUserWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        Response<CommunicationIdentityAccessTokenResult> response =
            client.createWithResponseAsync(new CommunicationIdentityCreateRequest(), context).block();

        if (response == null || response.getValue() == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Service failed to return a response or expected value."));
        }
        String id = response.getValue().getIdentity().getId();
        return new SimpleResponse<CommunicationUserIdentifier>(
            response,
            new CommunicationUserIdentifier(id));

    }

    /**
     * Creates a new CommunicationUserIdentifier with token.
     *
     * @param scopes The list of scopes for the token.
     * @return The created communication user and token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationUserIdentifierAndToken createUserAndToken(
        Iterable<CommunicationTokenScope> scopes) {
        Objects.requireNonNull(scopes);
        final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());
        CommunicationIdentityAccessTokenResult result = client.create(
            new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopesInput));
        return userWithAccessTokenResultConverter(result);
    }

    /**
     * Creates a new CommunicationUserIdentifier with token with response.
     *
     * @param scopes The list of scopes for the token.
     * @param context A {@link Context} representing the request context.
     * @return The created communication user and token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationUserIdentifierAndToken> createUserAndTokenWithResponse(
        Iterable<CommunicationTokenScope> scopes, Context context) {
        Objects.requireNonNull(scopes);
        context = context == null ? Context.NONE : context;
        final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());
        Response<CommunicationIdentityAccessTokenResult> response = client.createWithResponseAsync(
            new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopesInput), context).block();

        if (response == null || response.getValue() == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Service failed to return a response or expected value."));
        }
        return new SimpleResponse<CommunicationUserIdentifierAndToken>(
            response,
            userWithAccessTokenResultConverter(response.getValue()));
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data.
     *
     * @param communicationUser The user to be deleted.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteUser(CommunicationUserIdentifier communicationUser) {
        Objects.requireNonNull(communicationUser);
        client.deleteAsync(communicationUser.getId()).block();
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data with response.
     *
     * @param communicationUser The user to be deleted.
     * @param context A {@link Context} representing the request context.
     * @return The response with void.
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void revokeTokens(CommunicationUserIdentifier communicationUser) {
        Objects.requireNonNull(communicationUser);
        client.revokeAccessTokensAsync(communicationUser.getId()).block();
    }

    /**
     * Revokes all the tokens created for a user before a specific date.
     *
     * @param communicationUser The user to be revoked token.
     * @param context the context of the request. Can also be null or
     *                          Context.NONE.
     * @return The response with void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> revokeTokensWithResponse(CommunicationUserIdentifier communicationUser, Context context) {
        Objects.requireNonNull(communicationUser);
        context = context == null ? Context.NONE : context;
        return client.revokeAccessTokensWithResponseAsync(communicationUser.getId(), context).block();
    }

    /**
     * Gets a token for an identity.
     *
     * @param communicationUser The user to be issued tokens.
     * @param scopes The scopes that the token should have.
     * @return the token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessToken getToken(CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes) {
        Objects.requireNonNull(communicationUser);
        Objects.requireNonNull(scopes);
        final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());
        CommunicationIdentityAccessToken rawToken = client.issueAccessToken(
            communicationUser.getId(),
            new CommunicationIdentityAccessTokenRequest().setScopes(scopesInput));
        return new AccessToken(rawToken.getToken(), rawToken.getExpiresOn());
    }

    /**
     * Gets a token for an identity.
     *
     * @param communicationUser The CommunicationUser from whom to issue a token.
     * @param scopes The scopes that the token should have.
     * @param context the context of the request. Can also be null or
     *                          Context.NONE.
     * @return the token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccessToken> getTokenWithResponse(CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes, Context context) {
        Objects.requireNonNull(communicationUser);
        Objects.requireNonNull(scopes);
        context = context == null ? Context.NONE : context;
        final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());
        Response<CommunicationIdentityAccessToken> response = client.issueAccessTokenWithResponseAsync(
            communicationUser.getId(),
            new CommunicationIdentityAccessTokenRequest().setScopes(scopesInput),
            context)
            .block();

        if (response == null || response.getValue() == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Service failed to return a response or expected value."));
        }

        return new SimpleResponse<AccessToken>(
            response,
            new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn()));
    }

    private CommunicationUserIdentifierAndToken userWithAccessTokenResultConverter(
        CommunicationIdentityAccessTokenResult identityAccessTokenResult) {
        CommunicationUserIdentifier user =
            new CommunicationUserIdentifier(identityAccessTokenResult.getIdentity().getId());
        AccessToken token = new AccessToken(
            identityAccessTokenResult.getAccessToken().getToken(),
            identityAccessTokenResult.getAccessToken().getExpiresOn());
        return new CommunicationUserIdentifierAndToken(user, token);

    }
}
