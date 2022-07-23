// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.implementation.CommunicationIdentitiesImpl;
import com.azure.communication.identity.implementation.CommunicationIdentityClientImpl;
import com.azure.communication.identity.implementation.converters.IdentityErrorConverter;
import com.azure.communication.identity.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenResult;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessToken;
import com.azure.communication.identity.models.*;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Asynchronous client interface for Azure Communication Services Identity
 * operations
 *
 * <p><strong>Instantiating an asynchronous Azure Communication Service Identity Client</strong></p>
 *
 * <!-- src_embed readme-sample-createCommunicationIdentityAsyncClient -->
 * <pre>
 * &#47;&#47; You can find your endpoint and access key from your resource in the Azure Portal
 * String endpoint = &quot;https:&#47;&#47;&lt;RESOURCE_NAME&gt;.communication.azure.com&quot;;
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;&quot;&lt;access-key&gt;&quot;&#41;;
 *
 * CommunicationIdentityAsyncClient communicationIdentityAsyncClient = new CommunicationIdentityClientBuilder&#40;&#41;
 *         .endpoint&#40;endpoint&#41;
 *         .credential&#40;keyCredential&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-createCommunicationIdentityAsyncClient -->
 *
 *<p>View {@link CommunicationIdentityClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CommunicationIdentityClientBuilder
 */
@ServiceClient(builder = CommunicationIdentityClientBuilder.class, isAsync = true)
public final class CommunicationIdentityAsyncClient {

    private final CommunicationIdentitiesImpl client;
    private final ClientLogger logger = new ClientLogger(CommunicationIdentityAsyncClient.class);

    CommunicationIdentityAsyncClient(CommunicationIdentityClientImpl communicationIdentityServiceClient) {
        client = communicationIdentityServiceClient.getCommunicationIdentities();
    }

    /**
     * Creates a new CommunicationUserIdentifier.
     *
     * @return The created communication user.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationUserIdentifier> createUser() {
        try {
            return client.createAsync(new CommunicationIdentityCreateRequest())
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap(
                    (CommunicationIdentityAccessTokenResult result) -> {
                        return Mono.just(new CommunicationUserIdentifier(result.getIdentity().getId()));
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new CommunicationUserIdentifier with response.
     *
     * @return The created communication user with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationUserIdentifier>> createUserWithResponse() {
        try {
            return client.createWithResponseAsync(new CommunicationIdentityCreateRequest())
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap(
                    (Response<CommunicationIdentityAccessTokenResult> response) -> {
                        String id = response.getValue().getIdentity().getId();
                        return Mono.just(
                            new SimpleResponse<CommunicationUserIdentifier>(
                                response,
                                new CommunicationUserIdentifier(id)));
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new CommunicationUserIdentifier with token.
     *
     * @param scopes The list of scopes for the token.
     * @return The created communication user and token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationUserIdentifierAndToken>
        createUserAndToken(Iterable<CommunicationTokenScope> scopes) {
        try {
            Objects.requireNonNull(scopes);
            final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());
            return client.createAsync(new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopesInput))
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap(
                    (CommunicationIdentityAccessTokenResult result) -> {
                        return Mono.just(userWithAccessTokenResultConverter(result));
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a new CommunicationUserIdentifier with token with response.
     *
     * @param scopes The list of scopes for the token.
     * @return The result with created communication user and token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationUserIdentifierAndToken>>
        createUserAndTokenWithResponse(Iterable<CommunicationTokenScope> scopes) {
        try {
            Objects.requireNonNull(scopes);
            final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(scopes.spliterator(), false).collect(Collectors.toList());
            return client.createWithResponseAsync(
                new CommunicationIdentityCreateRequest().setCreateTokenWithScopes(scopesInput))
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap(
                    (Response<CommunicationIdentityAccessTokenResult> response) -> {
                        return Mono.just(new SimpleResponse<CommunicationUserIdentifierAndToken>(response,
                            userWithAccessTokenResultConverter(response.getValue())));
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data.
     *
     * @param communicationUser The user to be deleted.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteUser(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return client.deleteAsync(communicationUser.getId())
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a CommunicationUserIdentifier, revokes its tokens and deletes its
     * data with response.
     *
     * @param communicationUser The user to be deleted.
     * @return The response with void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteUserWithResponse(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return client.deleteWithResponseAsync(communicationUser.getId())
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Revokes all the tokens created for an identifier.
     *
     * @param communicationUser The user to be revoked access tokens.
     * @return A reactive response signalling completion.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> revokeTokens(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return client.revokeAccessTokensAsync(communicationUser.getId())
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Revokes all the tokens created for an identifier with response.
     *
     * @param communicationUser The user to be revoked tokens.
     * @return The response with void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> revokeTokensWithResponse(CommunicationUserIdentifier communicationUser) {
        try {
            Objects.requireNonNull(communicationUser);
            return client.revokeAccessTokensWithResponseAsync(communicationUser.getId())
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a Communication Identity access token for a {@link CommunicationUserIdentifier}.
     *
     * @param getTokenOptions {@link GetTokenOptions} to pass mandatory and configurable parameters to get a Communication
     * Identity access token for a {@link CommunicationUserIdentifier}.
     * @return the Communication Identity access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessToken> getToken(GetTokenOptions getTokenOptions) {
        try {
            Objects.requireNonNull(getTokenOptions.getCommunicationUser());
            Objects.requireNonNull(getTokenOptions.getCommunicationUser());
            final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(getTokenOptions.getScopes().spliterator(), false).collect(Collectors.toList());

            CommunicationIdentityAccessTokenRequest tokenRequest = new CommunicationIdentityAccessTokenRequest();
            tokenRequest.setScopes(scopesInput);

            if (getTokenOptions.getExpiresInMinutes() != null) {
                Integer expiresInMinutes = Math.toIntExact(getTokenOptions.getExpiresInMinutes().toMinutes());
                tokenRequest.setExpiresInMinutes(expiresInMinutes);
            }

            return client.issueAccessTokenAsync(getTokenOptions.getCommunicationUser().getId(),
                    tokenRequest
                )
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap((CommunicationIdentityAccessToken rawToken) -> {
                    return Mono.just(new AccessToken(rawToken.getToken(), rawToken.getExpiresOn()));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<AccessToken> getToken(CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes) {
        GetTokenOptions getTokenOptions = new GetTokenOptions(communicationUser, scopes);
        return getToken(getTokenOptions);
    }

    /**
     * Gets a Communication Identity access token for a {@link CommunicationUserIdentifier}.
     *
     * @param getTokenOptions {@link GetTokenOptions} to pass mandatory and configurable parameters to get a Communication
     * Identity access token for a {@link CommunicationUserIdentifier}.
     * @return the Communication Identity access token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessToken>> getTokenWithResponse(GetTokenOptions getTokenOptions) {
        try {
            Objects.requireNonNull(getTokenOptions.getCommunicationUser());
            Objects.requireNonNull(getTokenOptions.getScopes());
            final List<CommunicationTokenScope> scopesInput = StreamSupport.stream(getTokenOptions.getScopes().spliterator(), false).collect(Collectors.toList());

            CommunicationIdentityAccessTokenRequest tokenRequest = new CommunicationIdentityAccessTokenRequest();
            tokenRequest.setScopes(scopesInput);

            if (getTokenOptions.getExpiresInMinutes() != null) {
                Integer expiresInMinutes = Math.toIntExact(getTokenOptions.getExpiresInMinutes().toMinutes());
                tokenRequest.setExpiresInMinutes(expiresInMinutes);
            }

            return client.issueAccessTokenWithResponseAsync(getTokenOptions.getCommunicationUser().getId(),
                    tokenRequest
                )
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap((Response<CommunicationIdentityAccessToken> response) -> {
                    AccessToken token = new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn());
                    return Mono.just(new SimpleResponse<AccessToken>(response, token));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a Communication Identity access token for a {@link CommunicationUserIdentifier}.
     *
     * @param communicationUser A {@link CommunicationUserIdentifier} from whom to issue a Communication Identity
     * access token.
     * @param scopes List of {@link CommunicationTokenScope} scopes for the Communication Identity access token.
     * @return the Communication Identity access token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessToken>> getTokenWithResponse(CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes) {
        GetTokenOptions getTokenOptions = new GetTokenOptions(communicationUser, scopes);
        return getTokenWithResponse(getTokenOptions);
    }

    /**
     * Converts CommunicationIdentityAccessTokenResult to CommunicationUserIdentifierAndToken
     *
     * @param identityAccessTokenResult The result input.
     * @return The result converted to CommunicationUserIdentifierAndToken type
     */
    private CommunicationUserIdentifierAndToken userWithAccessTokenResultConverter(
        CommunicationIdentityAccessTokenResult identityAccessTokenResult) {
        CommunicationUserIdentifier user =
            new CommunicationUserIdentifier(identityAccessTokenResult.getIdentity().getId());
        AccessToken token = new AccessToken(
            identityAccessTokenResult.getAccessToken().getToken(),
            identityAccessTokenResult.getAccessToken().getExpiresOn());
        return new CommunicationUserIdentifierAndToken(user, token);
    }

    private IdentityErrorResponseException translateException(CommunicationErrorResponseException exception) {
        IdentityError error = null;
        if (exception.getValue() != null) {
            error = IdentityErrorConverter.convert(exception.getValue().getError());
        }
        return new IdentityErrorResponseException(exception.getMessage(), exception.getResponse(), error);
    }

    /**
     * Exchanges an Azure AD access token of a Teams User for a new Communication Identity access token.
     *
     * @param options {@link GetTokenForTeamsUserOptions} request options used to exchange an Azure AD access token of a Teams User for a new Communication Identity access token.
     * @return Communication Identity access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessToken> getTokenForTeamsUser(GetTokenForTeamsUserOptions options) {
        try {
            return client.exchangeTeamsUserAccessTokenAsync(options)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap((CommunicationIdentityAccessToken rawToken) -> {
                    return Mono.just(new AccessToken(rawToken.getToken(), rawToken.getExpiresOn()));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Exchanges an Azure AD access token of a Teams User for a new Communication Identity access token.
     *
     * @param options {@link GetTokenForTeamsUserOptions} request options used to exchange an Azure AD access token of a Teams User for a new Communication Identity access token.
     * @return Communication Identity access token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessToken>> getTokenForTeamsUserWithResponse(GetTokenForTeamsUserOptions options) {
        try {
            return client.exchangeTeamsUserAccessTokenWithResponseAsync(options)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap((Response<CommunicationIdentityAccessToken> response) -> {
                    AccessToken token = new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn());
                    return Mono.just(new SimpleResponse<AccessToken>(response, token));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

}
