// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.implementation.CommunicationIdentitiesImpl;
import com.azure.communication.identity.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessToken;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenResult;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.communication.identity.models.GetTokenForTeamsUserOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

/**
 * Synchronous client interface for Azure Communication Service Identity operations
 *
 * <p><strong>Instantiating a synchronous Azure Communication Service Identity Client</strong></p>
 *
 * <!-- src_embed readme-sample-createCommunicationIdentityClient -->
 * <pre>
 * &#47;&#47; You can find your endpoint and access key from your resource in the Azure Portal
 * String endpoint = &quot;https:&#47;&#47;&lt;RESOURCE_NAME&gt;.communication.azure.com&quot;;
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;&quot;&lt;access-key&gt;&quot;&#41;;
 *
 * CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder&#40;&#41;
 *     .endpoint&#40;endpoint&#41;
 *     .credential&#40;keyCredential&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createCommunicationIdentityClient -->
 *
 *<p>View {@link CommunicationIdentityClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CommunicationIdentityClientBuilder
 */
@ServiceClient(builder = CommunicationIdentityClientBuilder.class, isAsync = false)
public final class CommunicationIdentityClient {

    private final CommunicationIdentitiesImpl client;
    private final ClientLogger logger = new ClientLogger(CommunicationIdentityClient.class);

    CommunicationIdentityClient(CommunicationIdentityClientImpl communicationIdentityClient) {
        client = communicationIdentityClient.getCommunicationIdentities();
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
            client.createWithResponse(new CommunicationIdentityCreateRequest(), context);

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
     * @param tokenExpiresIn Custom validity period of the Communication Identity access token within [1,24]
     * hours range. If not provided, the default value of 24 hours will be used.
     * @return The created communication user and token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationUserIdentifierAndToken createUserAndToken(
        Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn) {
        Objects.requireNonNull(scopes);

        CommunicationIdentityCreateRequest communicationIdentityCreateRequest =
            CommunicationIdentityClientUtils.createCommunicationIdentityCreateRequest(scopes, tokenExpiresIn, logger);

        CommunicationIdentityAccessTokenResult result = client.create(communicationIdentityCreateRequest);
        return userWithAccessTokenResultConverter(result);
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
        return createUserAndToken(scopes, null);
    }

    /**
     * Creates a new CommunicationUserIdentifier with token with response.
     *
     * @param scopes The list of scopes for the token.
     * @param tokenExpiresIn Custom validity period of the Communication Identity access token within [1,24]
     * hours range. If not provided, the default value of 24 hours will be used.
     * @param context A {@link Context} representing the request context.
     * @return The created communication user and token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationUserIdentifierAndToken> createUserAndTokenWithResponse(
        Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn, Context context) {
        Objects.requireNonNull(scopes);
        context = context == null ? Context.NONE : context;

        CommunicationIdentityCreateRequest communicationIdentityCreateRequest =
            CommunicationIdentityClientUtils.createCommunicationIdentityCreateRequest(scopes, tokenExpiresIn, logger);

        Response<CommunicationIdentityAccessTokenResult> response = client.createWithResponse(
            communicationIdentityCreateRequest, context);

        if (response == null || response.getValue() == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Service failed to return a response or expected value."));
        }
        return new SimpleResponse<CommunicationUserIdentifierAndToken>(
            response,
            userWithAccessTokenResultConverter(response.getValue()));
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
        return createUserAndTokenWithResponse(scopes, null, context);
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
        client.delete(communicationUser.getId());
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
        return client.deleteWithResponse(communicationUser.getId(), context);
    }

    /**
     * Revokes all the tokens created for an identifier.
     *
     * @param communicationUser The user to be revoked token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void revokeTokens(CommunicationUserIdentifier communicationUser) {
        Objects.requireNonNull(communicationUser);
        client.revokeAccessTokens(communicationUser.getId());
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
        return client.revokeAccessTokensWithResponse(communicationUser.getId(), context);
    }

    /**
     * Gets a Communication Identity access token for a {@link CommunicationUserIdentifier}.
     *
     * @param communicationUser A {@link CommunicationUserIdentifier} from whom to issue a Communication Identity
     * access token.
     * @param scopes List of {@link CommunicationTokenScope} scopes for the Communication Identity access token.
     * @param tokenExpiresIn Custom validity period of the Communication Identity access token within [1,24]
     * hours range. If not provided, the default value of 24 hours will be used.
     * @return the Communication Identity access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessToken getToken(
        CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes,
        Duration tokenExpiresIn) {
        Objects.requireNonNull(communicationUser);
        Objects.requireNonNull(scopes);

        CommunicationIdentityAccessTokenRequest tokenRequest =
            CommunicationIdentityClientUtils.createCommunicationIdentityAccessTokenRequest(scopes, tokenExpiresIn, logger);

        CommunicationIdentityAccessToken rawToken = client.issueAccessToken(
            communicationUser.getId(),
            tokenRequest);
        return new AccessToken(rawToken.getToken(), rawToken.getExpiresOn());
    }

    /**
     * Gets a Communication Identity access token for a {@link CommunicationUserIdentifier}.
     *
     * @param communicationUser A {@link CommunicationUserIdentifier} from whom to issue a Communication Identity
     * access token.
     * @param scopes List of {@link CommunicationTokenScope} scopes for the Communication Identity access token.
     * @return the Communication Identity access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessToken getToken(CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes) {
        return getToken(communicationUser, scopes, null);
    }

    /**
     * Gets a Communication Identity access token for a {@link CommunicationUserIdentifier}.
     *
     * @param communicationUser A {@link CommunicationUserIdentifier} from whom to issue a Communication Identity
     * access token.
     * @param scopes List of {@link CommunicationTokenScope} scopes for the Communication Identity access token.
     * @param tokenExpiresIn Custom validity period of the Communication Identity access token within [1,24]
     * hours range. If not provided, the default value of 24 hours will be used.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the Communication Identity access token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccessToken> getTokenWithResponse(
        CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes,
        Duration tokenExpiresIn,
        Context context) {
        Objects.requireNonNull(communicationUser);
        Objects.requireNonNull(scopes);
        context = context == null ? Context.NONE : context;

        CommunicationIdentityAccessTokenRequest tokenRequest =
            CommunicationIdentityClientUtils.createCommunicationIdentityAccessTokenRequest(scopes, tokenExpiresIn, logger);

        Response<CommunicationIdentityAccessToken> response = client.issueAccessTokenWithResponse(
                communicationUser.getId(),
                tokenRequest,
                context);

        if (response == null || response.getValue() == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Service failed to return a response or expected value."));
        }

        return new SimpleResponse<AccessToken>(
            response,
            new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn()));
    }

    /**
     * Gets a Communication Identity access token for a {@link CommunicationUserIdentifier}.
     *
     * @param communicationUser A {@link CommunicationUserIdentifier} from whom to issue a Communication Identity
     * access token.
     * @param scopes List of {@link CommunicationTokenScope} scopes for the Communication Identity access token.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the Communication Identity access token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccessToken> getTokenWithResponse(CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes, Context context) {
        return getTokenWithResponse(communicationUser, scopes, null, context);
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

    /**
     * Exchanges an Azure AD access token of a Teams User for a new Communication Identity access token.
     *
     * @param options {@link GetTokenForTeamsUserOptions} request options used to exchange an Azure AD access token of a Teams User for a new Communication Identity access token.
     * @return Communication Identity access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AccessToken getTokenForTeamsUser(GetTokenForTeamsUserOptions options) {
        CommunicationIdentityAccessToken rawToken = client.exchangeTeamsUserAccessToken(options);
        return new AccessToken(rawToken.getToken(), rawToken.getExpiresOn());
    }

    /**
     * Exchanges an Azure AD access token of a Teams User for a new Communication Identity access token.
     *
     * @param options {@link GetTokenForTeamsUserOptions} request options used to exchange an Azure AD access token of a Teams User for a new Communication Identity access token.
     * @param context the context of the request. Can also be null or
     *                          Context.NONE.
     * @return Communication Identity access token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AccessToken> getTokenForTeamsUserWithResponse(GetTokenForTeamsUserOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        Response<CommunicationIdentityAccessToken> response =  client.exchangeTeamsUserAccessTokenWithResponse(options, context);
        if (response == null || response.getValue() == null) {
            throw logger.logExceptionAsError(new IllegalStateException("Service failed to return a response or expected value."));
        }

        return new SimpleResponse<AccessToken>(
            response,
            new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn()));
    }
}
