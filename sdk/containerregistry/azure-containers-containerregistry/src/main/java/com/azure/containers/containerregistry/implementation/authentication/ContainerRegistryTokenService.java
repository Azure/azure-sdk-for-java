// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.ContainerRegistryServiceVersion;
import com.azure.containers.containerregistry.implementation.models.TokenGrantType;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

/**
 * A token service for obtaining tokens to be used by the container registry service.
 */
public class ContainerRegistryTokenService implements TokenCredential {
    private AccessTokenCacheImpl refreshTokenCache;
    private TokenServiceImpl tokenService;
    private boolean isAnonymousAccess;
    private final ClientLogger logger = new ClientLogger(ContainerRegistryTokenService.class);

    /**
     * Creates an instance of AccessTokenCache with default scheme "Bearer".
     *
     * @param aadTokenCredential the credential to be used to acquire the token.
     * @param url the container registry endpoint.
     * @param serviceVersion the service api version being targeted by the client.
     * @param pipeline the pipeline to be used for the rest calls to the service.
     * @param serializerAdapter the serializer adapter to be used for the rest calls to the service.
     */
    public ContainerRegistryTokenService(TokenCredential aadTokenCredential, ContainerRegistryAudience audience,
                                         String url, ContainerRegistryServiceVersion serviceVersion,
                                         HttpPipeline pipeline, SerializerAdapter serializerAdapter) {
        this.tokenService = new TokenServiceImpl(url, serviceVersion, pipeline, serializerAdapter);

        if (aadTokenCredential != null) {
            this.refreshTokenCache = new AccessTokenCacheImpl(
                new ContainerRegistryRefreshTokenCredential(tokenService, aadTokenCredential, audience));
        } else {
            isAnonymousAccess = true;
        }
    }

    ContainerRegistryTokenService setTokenService(TokenServiceImpl tokenServiceImpl) {
        this.tokenService = tokenServiceImpl;
        return this;
    }

    ContainerRegistryTokenService setRefreshTokenCache(AccessTokenCacheImpl tokenCache) {
        this.refreshTokenCache = tokenCache;
        return this;
    }

    ContainerRegistryTokenService setAnonymousAccess(boolean isAnonymousAccess) {
        this.isAnonymousAccess = isAnonymousAccess;
        return this;
    }

    /**
     * Gets a token against the token request context.
     *
     * @param tokenRequestContext the token request context to be used to get the token.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
        if (!(tokenRequestContext instanceof ContainerRegistryTokenRequestContext)) {
            logger.info("tokenRequestContext is not of the type ContainerRegistryTokenRequestContext");
            return Mono.empty();
        }

        ContainerRegistryTokenRequestContext requestContext =
            (ContainerRegistryTokenRequestContext) tokenRequestContext;

        String scope = requestContext.getScope();
        String serviceName = requestContext.getServiceName();

        return Mono.defer(() -> {
            if (this.isAnonymousAccess) {
                return this.tokenService.getAcrAccessTokenAsync(null, scope, serviceName, TokenGrantType.PASSWORD);
            }

            return this.refreshTokenCache.getToken(requestContext)
                .flatMap(refreshToken -> this.tokenService.getAcrAccessTokenAsync(refreshToken.getToken(), scope,
                    serviceName, TokenGrantType.REFRESH_TOKEN));
        }).doOnError(err -> logger.error("Could not fetch the ACR error token.", err));
    }

    public AccessToken getTokenSync(TokenRequestContext tokenRequestContext) {
        return this.getToken(tokenRequestContext).block();
    }
}
