// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

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
import com.azure.core.util.logging.ClientLogger;

import java.util.List;
import java.util.Objects;

import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Asynchronous client interface for Azure Communication Services identity
 * operations
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
     * @return the created communication user.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationUserIdentifier> createUser() {
        try {
            return client.createAsync(new CommunicationIdentityCreateRequest())
                .flatMap(
                    (CommunicationIdentityAccessTokenResult result) -> {
                        if (result.getIdentity() != null) {
                            return Mono.just(new CommunicationUserIdentifier(result.getIdentity().getId()));
                        }
                        return Mono.empty();
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new CommunicationUserIdentifier with response.
     *
     * @return the created communication user with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationUserIdentifier>> createUserWithResponse() {
        try {
            return client.createWithResponseAsync(new CommunicationIdentityCreateRequest())
                .flatMap(
                    (Response<CommunicationIdentityAccessTokenResult> response) -> {
                        if (response != null && response.getValue() != null && response.getValue().getIdentity() != null) {
                            String id = response.getValue().getIdentity().getId();
                            return Mono.just(
                                new SimpleResponse<CommunicationUserIdentifier>(
                                    response,
                                    new CommunicationUserIdentifier(id)));
                        }
                        return Mono.empty();
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new CommunicationUserIdentifier with token.
     *
     * @param scopes the list of scopes for the token.
     * @return the result with created communication user and token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationUserIdentifierWithTokenResult> 
        createUserWithToken(List<CommunicationTokenScope> scopes) {
        try {
            Objects.requireNonNull(scopes);
            return client.createAsync(new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopes))
                .flatMap(
                    (CommunicationIdentityAccessTokenResult result) -> {
                        if (result != null && result.getIdentity() != null && result.getAccessToken() != null) {
                            CommunicationUserIdentifier user = 
                                new CommunicationUserIdentifier(result.getIdentity().getId());
                            AccessToken token = 
                                new AccessToken(result.getAccessToken().getToken(), result.getAccessToken().getExpiresOn());
                            return Mono.just(
                                new CommunicationUserIdentifierWithTokenResult(user, token));
                        }
                        return Mono.empty();
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new CommunicationUserIdentifier with token with response.
     *
     * @param scopes the list of scopes for the token.
     * @return the result with created communication user and token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationUserIdentifierWithTokenResult>> 
        createUserWithTokenWithResponse(List<CommunicationTokenScope> scopes) {
        try {
            Objects.requireNonNull(scopes);
            return client.createWithResponseAsync(
                new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopes))
                .flatMap(
                    (Response<CommunicationIdentityAccessTokenResult> response) -> {
                        if (response != null && response.getValue() != null
                            && response.getValue().getIdentity() != null && response.getValue().getAccessToken() != null) {
                            String id = response.getValue().getIdentity().getId();
                            CommunicationUserIdentifier user = new CommunicationUserIdentifier(id);
                            CommunicationIdentityAccessToken rawToken = response.getValue().getAccessToken();
                            AccessToken token = new AccessToken(rawToken.getToken(), rawToken.getExpiresOn());
                            return Mono.just(new SimpleResponse<CommunicationUserIdentifierWithTokenResult>(response, 
                                new CommunicationUserIdentifierWithTokenResult(user, token)));
                        }
                        return Mono.empty();
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data.
     *
     * @param communicationUser the user to be deleted.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteUser(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return client.deleteAsync(communicationUser.getId());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }    
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data with response.
     * 
     * @return the response.
     * @param communicationUser The user to be deleted.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteUserWithResponse(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return client.deleteWithResponseAsync(communicationUser.getId());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }   
    }

    /**
     * Revokes all the tokens created for an identifier.
     * 
     * @param communicationUser The user to be revoked access tokens.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> revokeTokens(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return client.revokeAccessTokensAsync(communicationUser.getId());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Revokes all the tokens created for an identifier with response.
     *
     * @param communicationUser The user to be revoked tokens.
     * @return the response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> revokeTokensWithResponse(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return client.revokeAccessTokensWithResponseAsync(communicationUser.getId());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Generates a new token for an identity.
     *
     * @param communicationUser The user to be issued tokens.
     * @param scopes The scopes that the token should have.
     * @return the issued token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessToken> issueToken(CommunicationUserIdentifier communicationUser,
        List<CommunicationTokenScope> scopes) {
        try {
            Objects.requireNonNull(communicationUser);
            Objects.requireNonNull(scopes);
            return client.issueAccessTokenAsync(communicationUser.getId(),
                new CommunicationIdentityAccessTokenRequest().setScopes(scopes))
                .flatMap((CommunicationIdentityAccessToken rawToken) -> {
                    return Mono.just(new AccessToken(rawToken.getToken(), rawToken.getExpiresOn()));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Generates a new token for an identity with response.
     *
     * @param communicationUser The user to be issued tokens.
     * @param scopes The scopes that the token should have.
     * @return the issued token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessToken>> issueTokenWithResponse(CommunicationUserIdentifier communicationUser,
        List<CommunicationTokenScope> scopes) {
        try {
            Objects.requireNonNull(communicationUser);
            Objects.requireNonNull(scopes);
            return client.issueAccessTokenWithResponseAsync(communicationUser.getId(),
                new CommunicationIdentityAccessTokenRequest().setScopes(scopes))
                .flatMap((Response<CommunicationIdentityAccessToken> response) -> {
                    if (response != null && response.getValue() != null) {
                        AccessToken token = new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn());
                        return Mono.just(new SimpleResponse<AccessToken>(response, token));
                    }
                    return Mono.empty();
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
