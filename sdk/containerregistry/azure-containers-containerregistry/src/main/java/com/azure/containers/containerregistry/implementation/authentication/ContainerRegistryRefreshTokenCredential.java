// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

/**
 * Token credentials representing the container registry refresh token.
 * This token is unique per registry operation.
 */
public class ContainerRegistryRefreshTokenCredential {

    private final TokenCredential aadTokenCredential;
    private final TokenServiceImpl tokenService;
    public static final String AAD_DEFAULT_SCOPE = "https://management.core.windows.net/.default";

    /**
     * Creates an instance of RefreshTokenCredential with default scheme "Bearer".
     * @param tokenService the container registry token service that calls the token rest APIs.
     * @param aadTokenCredential the ARM access token.
     */
    ContainerRegistryRefreshTokenCredential(TokenServiceImpl tokenService, TokenCredential aadTokenCredential) {
        this.tokenService = tokenService;
        this.aadTokenCredential = aadTokenCredential;
    }

    /**
     * Creates the container registry refresh token for the given context.
     *
     * @param context the context for the token to be generated.
     */
    public Mono<AccessToken> getToken(ContainerRegistryTokenRequestContext context) {
        String serviceName = context.getServiceName();

        return Mono.defer(() -> aadTokenCredential.getToken(new TokenRequestContext().addScopes(AAD_DEFAULT_SCOPE))
            .flatMap(token -> this.tokenService.getAcrRefreshTokenAsync(token.getToken(), serviceName)));
    }

}
