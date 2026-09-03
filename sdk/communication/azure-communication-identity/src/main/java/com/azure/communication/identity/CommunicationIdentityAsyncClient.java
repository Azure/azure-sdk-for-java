// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.communication.identity.implementation.IdentityClientImpl;
import com.azure.communication.identity.implementation.converters.IdentityErrorConverter;
import com.azure.communication.identity.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessTokenResult;
import com.azure.communication.identity.implementation.models.CommunicationIdentityCreateRequest;
import com.azure.communication.identity.implementation.models.CommunicationIdentityAccessToken;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.identity.models.CommunicationUserIdentifierAndToken;
import com.azure.communication.identity.models.GetTokenForTeamsUserOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

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

    private final IdentityOperationsAsyncClient client;
    private final TeamsUserOperationsAsyncClient teamsUserClient;
    private final ClientLogger logger = new ClientLogger(CommunicationIdentityAsyncClient.class);

    CommunicationIdentityAsyncClient(IdentityClientImpl identityClient) {
        client = new IdentityOperationsAsyncClient(identityClient.getIdentityOperations());
        teamsUserClient = new TeamsUserOperationsAsyncClient(identityClient.getTeamsUserOperations());
    }

    /**
     * Deserializes the {@link BinaryData} body returned by a generated protocol method, preserving the
     * status code and headers of the original response.
     */
    private static <T> Response<T> mapResponse(Response<BinaryData> response, Class<T> type) {
        BinaryData value = response.getValue();
        return new SimpleResponse<>(response, value == null ? null : value.toObject(type));
    }

    /**
     * Calls the generated create protocol method. The request body is carried on {@link RequestOptions}
     * because the generated signature takes no body parameter.
     */
    private Mono<Response<CommunicationIdentityAccessTokenResult>>
        createWithResponseInternal(CommunicationIdentityCreateRequest body) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setBody(BinaryData.fromObject(body));
        return client.createWithResponse(requestOptions)
            .map(response -> mapResponse(response, CommunicationIdentityAccessTokenResult.class));
    }

    /**
     * Creates a new CommunicationUserIdentifier.
     *
     * @return The created communication user.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationUserIdentifier> createUser() {
        try {
            return client.create(new CommunicationIdentityCreateRequest())
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException)
                .flatMap((CommunicationIdentityAccessTokenResult result) -> {
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
            return createWithResponseInternal(new CommunicationIdentityCreateRequest())
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException)
                .flatMap((Response<CommunicationIdentityAccessTokenResult> response) -> {
                    String id = response.getValue().getIdentity().getId();
                    return Mono.just(
                        new SimpleResponse<CommunicationUserIdentifier>(response, new CommunicationUserIdentifier(id)));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<CommunicationUserIdentifierAndToken> createUserAndToken(Iterable<CommunicationTokenScope> scopes,
        Duration tokenExpiresIn) {
        try {
            Objects.requireNonNull(scopes);

            CommunicationIdentityCreateRequest communicationIdentityCreateRequest = CommunicationIdentityClientUtils
                .createCommunicationIdentityCreateRequest(scopes, tokenExpiresIn, logger);

            return client.create(communicationIdentityCreateRequest)
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException)
                .flatMap((CommunicationIdentityAccessTokenResult result) -> {
                    return Mono.just(userWithAccessTokenResultConverter(result));
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
    public Mono<CommunicationUserIdentifierAndToken> createUserAndToken(Iterable<CommunicationTokenScope> scopes) {
        return createUserAndToken(scopes, null);
    }

    /**
     * Creates a new CommunicationUserIdentifier with token with response.
     *
     * @param scopes The list of scopes for the token.
     * @param tokenExpiresIn Custom validity period of the Communication Identity access token within [1,24]
     * hours range. If not provided, the default value of 24 hours will be used.
     * @return The result with created communication user and token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationUserIdentifierAndToken>>
        createUserAndTokenWithResponse(Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn) {
        try {
            Objects.requireNonNull(scopes);

            CommunicationIdentityCreateRequest communicationIdentityCreateRequest = CommunicationIdentityClientUtils
                .createCommunicationIdentityCreateRequest(scopes, tokenExpiresIn, logger);

            return createWithResponseInternal(communicationIdentityCreateRequest)
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException)
                .flatMap((Response<CommunicationIdentityAccessTokenResult> response) -> {
                    return Mono.just(new SimpleResponse<CommunicationUserIdentifierAndToken>(response,
                        userWithAccessTokenResultConverter(response.getValue())));
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
        return createUserAndTokenWithResponse(scopes, null);
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
            return client.delete(communicationUser.getId())
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException);
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
            return client.deleteWithResponse(communicationUser.getId(), new RequestOptions())
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException);
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
            return client.revokeAccessTokens(communicationUser.getId())
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException);
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
            return client.revokeAccessTokensWithResponse(communicationUser.getId(), new RequestOptions())
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException);
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
     * @param tokenExpiresIn Custom validity period of the Communication Identity access token within [1,24]
     * hours range. If not provided, the default value of 24 hours will be used.
     * @return the Communication Identity access token.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AccessToken> getToken(CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn) {
        try {
            Objects.requireNonNull(communicationUser);
            Objects.requireNonNull(scopes);

            CommunicationIdentityAccessTokenRequest tokenRequest = CommunicationIdentityClientUtils
                .createCommunicationIdentityAccessTokenRequest(scopes, tokenExpiresIn, logger);

            return client.issueAccessToken(communicationUser.getId(), tokenRequest)
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException)
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
     * @return the Communication Identity access token with response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AccessToken>> getTokenWithResponse(CommunicationUserIdentifier communicationUser,
        Iterable<CommunicationTokenScope> scopes, Duration tokenExpiresIn) {
        try {
            Objects.requireNonNull(communicationUser);
            Objects.requireNonNull(scopes);

            CommunicationIdentityAccessTokenRequest tokenRequest = CommunicationIdentityClientUtils
                .createCommunicationIdentityAccessTokenRequest(scopes, tokenExpiresIn, logger);

            return client
                .issueAccessTokenWithResponse(communicationUser.getId(), BinaryData.fromObject(tokenRequest),
                    new RequestOptions())
                .map(response -> mapResponse(response, CommunicationIdentityAccessToken.class))
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException)
                .flatMap((Response<CommunicationIdentityAccessToken> response) -> {
                    AccessToken token
                        = new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn());
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
        return getTokenWithResponse(communicationUser, scopes, null);
    }

    /**
     * Converts CommunicationIdentityAccessTokenResult to CommunicationUserIdentifierAndToken
     *
     * @param identityAccessTokenResult The result input.
     * @return The result converted to CommunicationUserIdentifierAndToken type
     */
    private CommunicationUserIdentifierAndToken
        userWithAccessTokenResultConverter(CommunicationIdentityAccessTokenResult identityAccessTokenResult) {
        CommunicationUserIdentifier user
            = new CommunicationUserIdentifier(identityAccessTokenResult.getIdentity().getId());
        AccessToken token = new AccessToken(identityAccessTokenResult.getAccessToken().getToken(),
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
    public Mono<AccessToken> getTokenForTeamsUser(GetTokenForTeamsUserOptions options) {
        try {
            return teamsUserClient.exchangeTeamsUserAccessToken(options)
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException)
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
            return teamsUserClient
                .exchangeTeamsUserAccessTokenWithResponse(BinaryData.fromObject(options), new RequestOptions())
                .map(response -> mapResponse(response, CommunicationIdentityAccessToken.class))
                .onErrorMap(CommunicationErrorResponseException.class, IdentityErrorConverter::translateException)
                .flatMap((Response<CommunicationIdentityAccessToken> response) -> {
                    AccessToken token
                        = new AccessToken(response.getValue().getToken(), response.getValue().getExpiresOn());
                    return Mono.just(new SimpleResponse<AccessToken>(response, token));
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
