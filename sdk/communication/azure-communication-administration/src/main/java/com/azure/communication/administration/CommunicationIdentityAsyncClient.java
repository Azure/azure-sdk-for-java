// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import com.azure.communication.administration.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.administration.implementation.CommunicationIdentityImpl;
import com.azure.communication.administration.models.CommunicationIdentity; 
import com.azure.communication.administration.models.CommunicationTokenRequest;
import com.azure.communication.administration.models.CommunicationIdentityUpdateRequest;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

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
     * Creates a new CommunicationUser.
     *
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationUserIdentifier> createUser() {
        try {
            return withContext(context -> createUser(context)
                .flatMap(
                    (Response<CommunicationUserIdentifier> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        }
                        return Mono.empty();
                    }));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new CommunicationUser.
     *
     * @return the created Communication User.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationUserIdentifier>> createUserWithResponse() {
        try {
            return withContext(context -> createUser(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new CommunicationUser.
     *
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the created Communication User.
     */
    public Mono<Response<CommunicationUserIdentifier>> createUser(Context context) {
        context = context == null ? Context.NONE : context;

        return client.createWithResponseAsync(context)
            .flatMap(
                (Response<CommunicationIdentity> res) -> {
                    if (res.getValue() != null) {
                        CommunicationUserIdentifier user = new CommunicationUserIdentifier(res.getValue().getId());
                        return Mono.just(new ResponseBase<HttpHeaders, CommunicationUserIdentifier>(res.getRequest(), 
                        res.getStatusCode(), res.getHeaders(), user, null));
                    } 
                    return Mono.empty();
                });
    }

    /**
     * Deletes a CommunicationUser, revokes its tokens and deletes its data.
     *
     * @param communicationUser The user to be deleted.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteUser(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return withContext(context -> deleteUser(communicationUser, context)
                .flatMap(
                    (Response<Void> res) -> {
                        return Mono.empty();
                    }));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }    
    }

    /**
     * Deletes a CommunicationUser, revokes its tokens and deletes its data.
     *
     * @param communicationUser The user to be deleted.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteUserWithResponse(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return withContext(context -> deleteUser(communicationUser, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }    
    }

    /**
     * Deletes a CommunicationUser, revokes its tokens and deletes its data.
     *
     * @param communicationUser The user to be deleted.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the response.
     */
    public Mono<Response<Void>> deleteUser(CommunicationUserIdentifier communicationUser, Context context) {
        context = context == null ? Context.NONE : context;

        return client.deleteWithResponseAsync(communicationUser.getId(), context);
    }

    /**
     * Revokes all the tokens created for a user before a specific date.
     *
     * @param communicationUser The CommunicationUser whose tokens will be revoked.
     * @param issuedBefore All tokens that are issued prior to this time should get revoked.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> revokeTokens(CommunicationUserIdentifier communicationUser, OffsetDateTime issuedBefore) {
        try {
            Objects.requireNonNull(communicationUser);
            return withContext(context -> revokeTokens(communicationUser, issuedBefore, context)
                .flatMap(
                    (Response<Void> res) -> {
                        return Mono.empty();
                    }));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Revokes all the tokens created for a user before a specific date.
     *
     * @param communicationUser The CommunicationUser whose tokens will be revoked.
     * @param issuedBefore All tokens that are issued prior to this time should get revoked.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> revokeTokensWithResponse(
        CommunicationUserIdentifier communicationUser, OffsetDateTime issuedBefore) {
        try {
            Objects.requireNonNull(communicationUser);
            return withContext(context -> revokeTokens(communicationUser, issuedBefore, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }    
    }

    /**
     * Revokes all the tokens created for a user before a specific date.
     *
     * @param communicationUser The CommunicationUser whose tokens will be revoked.
     * @param issuedBefore All tokens that are issued prior to this time should get revoked.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the response.
     */
    public Mono<Response<Void>> revokeTokens(
        CommunicationUserIdentifier communicationUser, OffsetDateTime issuedBefore, Context context) {
        context = context == null ? Context.NONE : context;
        if (issuedBefore == null) {
            issuedBefore = OffsetDateTime.now();
        }
        CommunicationIdentityUpdateRequest tokenRevocationRequest = new CommunicationIdentityUpdateRequest();
        tokenRevocationRequest.setTokensValidFrom(issuedBefore);
        return client.updateWithResponseAsync(communicationUser.getId(), tokenRevocationRequest, context);
    }

    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The CommunicationUser from whom to issue a token.
     * @param scopes The scopes that the token should have.
     * @return the object with the issued token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationUserToken> issueToken(
        CommunicationUserIdentifier communicationUser, List<String> scopes) {
        try {
            Objects.requireNonNull(communicationUser);
            Objects.requireNonNull(scopes);
            return withContext(context -> issueToken(communicationUser, scopes, context)
                .flatMap(
                    (Response<CommunicationUserToken> res) -> {
                        if (res.getValue() != null) {
                            return Mono.just(res.getValue());
                        }
                        return Mono.empty();
                    }));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The CommunicationUser from whom to issue a token. 
     * @param scopes The scopes that the token should have.
     * @return the object with the issued token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationUserToken>> issueTokenWithResponse(
        CommunicationUserIdentifier communicationUser, List<String> scopes) {
        try {
            Objects.requireNonNull(communicationUser);
            Objects.requireNonNull(scopes);
            return withContext(context -> issueToken(communicationUser, scopes, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }    
    }


    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The CommunicationUser from whom to issue a token. 
     * @param scopes The scopes that the token should have.
     * @param context the context of the request. Can also be null or Context.NONE.
     * @return the object with the issued token.
     */
    public Mono<Response<CommunicationUserToken>> issueToken(
        CommunicationUserIdentifier communicationUser, List<String> scopes, Context context) {
        context = context == null ? Context.NONE : context;

        CommunicationTokenRequest communicationTokenRequest = new CommunicationTokenRequest();
        communicationTokenRequest.setScopes(scopes);
        return client.issueTokenWithResponseAsync(communicationUser.getId(), communicationTokenRequest, context)
        .flatMap(res -> {
            if (res.getValue() != null) {
                CommunicationUserToken userToken = new CommunicationUserToken();
                userToken.setUser(new CommunicationUserIdentifier(res.getValue().getId()))
                    .setToken(res.getValue().getToken())
                    .setExpiresOn(res.getValue().getExpiresOn());
                return Mono.just(new ResponseBase<HttpHeaders, CommunicationUserToken>(res.getRequest(), 
                    res.getStatusCode(), res.getHeaders(), userToken, null));
            } 
            return Mono.empty();
        });
    }
}
