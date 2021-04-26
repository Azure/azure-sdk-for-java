// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

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
    private final ClientLogger logger = new ClientLogger(ContainerRegistryTokenService.class);

    /**
     * Creates an instance of AccessTokenCache with default scheme "Bearer".
     *
     * @param aadTokenCredential the credential to be used to acquire the token.
     * @param url                the container registry endpoint.
     * @param pipeline           the pipeline to be used for the rest calls to the service.
     * @param serializerAdapter  the serializer adapter to be used for the rest calls to the service.
     */
    public ContainerRegistryTokenService(TokenCredential aadTokenCredential, String url, HttpPipeline pipeline, SerializerAdapter serializerAdapter) {
        this.tokenService = new TokenServiceImpl(url, pipeline, serializerAdapter);
        this.refreshTokenCache = new AccessTokenCacheImpl(new ContainerRegistryRefreshTokenCredential(tokenService, aadTokenCredential));
    }

    ContainerRegistryTokenService setTokenService(TokenServiceImpl tokenServiceImpl) {
        this.tokenService = tokenServiceImpl;
        return this;
    }

    ContainerRegistryTokenService setRefreshTokenCache(AccessTokenCacheImpl tokenCache) {
        this.refreshTokenCache = tokenCache;
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

        ContainerRegistryTokenRequestContext requestContext = (ContainerRegistryTokenRequestContext) tokenRequestContext;

        String scope = requestContext.getScope();
        String serviceName = requestContext.getServiceName();

        return Mono.defer(() -> this.refreshTokenCache.getToken(requestContext)
            .flatMap(refreshToken -> this.tokenService.getAcrAccessTokenAsync(refreshToken.getToken(), scope, serviceName))
            .doOnError(err -> logger.error("Could not fetch the ACR error token.", err)));
    }
}
