// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.implementation.AuthenticationsImpl;
import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.models.AcrAccessToken;
import com.azure.containers.containerregistry.implementation.models.TokenGrantType;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A token service for obtaining tokens to be used by the container registry service.
 */
public class ContainerRegistryTokenService implements TokenCredential {
    private final AccessTokenCacheImpl refreshTokenCache;
    private final AuthenticationsImpl authenticationsImpl;
    private final boolean isAnonymousAccess;
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryTokenService.class);
    private static final Mono<AccessToken> ANONYMOUS_REFRESH_TOKEN
        = Mono.just(new AccessToken(null, OffsetDateTime.MAX));
    private final AtomicReference<String> lastAccessToken = new AtomicReference<>();

    /**
     * Creates an instance of AccessTokenCache with default scheme "Bearer".
     *
     * @param aadTokenCredential the credential to be used to acquire the token.
     * @param client AzureContainerRegistryImpl instance.
     */
    public ContainerRegistryTokenService(TokenCredential aadTokenCredential, ContainerRegistryAudience audience,
        AzureContainerRegistryImpl client) {
        this.authenticationsImpl = client.getAuthentications();

        if (aadTokenCredential != null) {
            this.refreshTokenCache = new AccessTokenCacheImpl(
                new ContainerRegistryRefreshTokenCredential(authenticationsImpl, aadTokenCredential, audience));
            this.isAnonymousAccess = false;
        } else {
            this.refreshTokenCache = null;
            this.isAnonymousAccess = true;
        }
    }

    ContainerRegistryTokenService(AuthenticationsImpl authenticationsImpl, AccessTokenCacheImpl tokenCache) {
        this.authenticationsImpl = authenticationsImpl;
        this.refreshTokenCache = tokenCache;
        this.isAnonymousAccess = false;
    }

    /**
     * Gets a token against the token request context.
     *
     * @param request the token request context to be used to get the token.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (!(request instanceof ContainerRegistryTokenRequestContext)) {
            return monoError(LOGGER, new IllegalArgumentException(
                "tokenRequestContext is not of the type ContainerRegistryTokenRequestContext"));
        }

        ContainerRegistryTokenRequestContext crRequest = (ContainerRegistryTokenRequestContext) request;

        Mono<AccessToken> getRefreshToken
            = isAnonymousAccess ? ANONYMOUS_REFRESH_TOKEN : refreshTokenCache.getToken(crRequest, true);
        TokenGrantType grantType = isAnonymousAccess ? TokenGrantType.PASSWORD : TokenGrantType.REFRESH_TOKEN;

        return getRefreshToken
            .flatMap(refreshToken -> authenticationsImpl.exchangeAcrRefreshTokenForAcrAccessTokenWithResponseAsync(
                crRequest.getServiceName(), crRequest.getScope(), refreshToken.getToken(), grantType, Context.NONE))
            .map(this::toAccessToken);
    }

    public AccessToken getTokenSync(TokenRequestContext tokenRequestContext) {
        if (!(tokenRequestContext instanceof ContainerRegistryTokenRequestContext)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "tokenRequestContext is not of the type ContainerRegistryTokenRequestContext"));
        }

        ContainerRegistryTokenRequestContext requestContext
            = (ContainerRegistryTokenRequestContext) tokenRequestContext;

        String refreshTokenString = null;
        TokenGrantType grantType = isAnonymousAccess ? TokenGrantType.PASSWORD : TokenGrantType.REFRESH_TOKEN;
        if (!isAnonymousAccess) {
            AccessToken refreshToken = refreshTokenCache.getTokenSync(requestContext, true);
            refreshTokenString = refreshToken.getToken();
        }

        return toAccessToken(authenticationsImpl.exchangeAcrRefreshTokenForAcrAccessTokenWithResponse(
            requestContext.getServiceName(), requestContext.getScope(), refreshTokenString, grantType, Context.NONE));
    }

    private AccessToken toAccessToken(Response<AcrAccessToken> response) {
        AcrAccessToken token = response.getValue();
        if (token != null) {
            String accessTokenStr = token.getAccessToken();
            lastAccessToken.set(accessTokenStr);
            return new AccessToken(accessTokenStr, JsonWebToken.retrieveExpiration(accessTokenStr));
        }

        throw LOGGER.logExceptionAsError(new ServiceResponseException("AcrAccessToken is missing in response."));
    }

    /**
     * Returns last token. Can be used for optimistic auth header setting before challenge is received.
     * It might have wrong scope or be expired - then usual challenge flow will happen.
     * In case of repetitive operations such as uploads/downloads, minimizes number of auth calls.
     */
    public String getLastToken() {
        return lastAccessToken.get();
    }
}
