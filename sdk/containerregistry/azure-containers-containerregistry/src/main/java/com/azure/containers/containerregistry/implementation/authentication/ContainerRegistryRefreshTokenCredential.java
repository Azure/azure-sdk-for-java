// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.implementation.AuthenticationsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrRefreshToken;
import com.azure.containers.containerregistry.implementation.models.PostContentSchemaGrantType;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ServiceResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Token credentials representing the container registry refresh token.
 * This token is unique per registry operation.
 */
public class ContainerRegistryRefreshTokenCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ContainerRegistryRefreshTokenCredential.class);
    private final TokenCredential aadTokenCredential;
    private final AuthenticationsImpl authenticationsImpl;
    private final TokenRequestContext tokenRequestContext;

    /**
     * Creates an instance of RefreshTokenCredential with default scheme "Bearer".
     * @param authenticationsImpl the container registry token service that calls the token rest APIs.
     * @param aadTokenCredential the ARM access token.
     */
    ContainerRegistryRefreshTokenCredential(AuthenticationsImpl authenticationsImpl, TokenCredential aadTokenCredential,
        ContainerRegistryAudience audience) {
        this.authenticationsImpl = authenticationsImpl;
        this.aadTokenCredential = aadTokenCredential;
        this.tokenRequestContext = new TokenRequestContext().addScopes(audience + "/.default");
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (!(request instanceof ContainerRegistryTokenRequestContext)) {
            return monoError(LOGGER,
                new IllegalArgumentException("Unexpected request type - " + request.getClass().getName()));
        }
        ContainerRegistryTokenRequestContext crRequest = (ContainerRegistryTokenRequestContext) request;
        return aadTokenCredential.getToken(tokenRequestContext)
            .flatMap(token -> authenticationsImpl.exchangeAadAccessTokenForAcrRefreshTokenWithResponseAsync(
                PostContentSchemaGrantType.ACCESS_TOKEN, crRequest.getServiceName(), null, null, token.getToken(),
                Context.NONE))
            .map(this::toAccessToken);
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        if (!(request instanceof ContainerRegistryTokenRequestContext)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Unexpected request type - " + request.getClass().getName()));
        }

        ContainerRegistryTokenRequestContext crRequest = (ContainerRegistryTokenRequestContext) request;

        AccessToken token = aadTokenCredential.getTokenSync(tokenRequestContext);
        Response<AcrRefreshToken> acrRefreshToken = authenticationsImpl
            .exchangeAadAccessTokenForAcrRefreshTokenWithResponse(PostContentSchemaGrantType.ACCESS_TOKEN,
                crRequest.getServiceName(), null, null, token.getToken(), Context.NONE);
        return toAccessToken(acrRefreshToken);
    }

    private AccessToken toAccessToken(Response<AcrRefreshToken> response) {
        AcrRefreshToken token = response.getValue();
        if (token != null) {
            String accessToken = token.getRefreshToken();
            return new AccessToken(accessToken, JsonWebToken.retrieveExpiration(accessToken));
        }

        throw LOGGER.logExceptionAsError(new ServiceResponseException("AcrRefreshToken is missing in response"));
    }
}
