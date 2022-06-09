// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.ContainerRegistryServiceVersion;
import com.azure.containers.containerregistry.implementation.models.AcrAccessToken;
import com.azure.containers.containerregistry.implementation.models.AcrRefreshToken;
import com.azure.containers.containerregistry.implementation.models.PostContentSchemaGrantType;
import com.azure.containers.containerregistry.implementation.models.TokenGrantType;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * Token service implementation that wraps the authentication rest APIs for ACR.
 */
public class TokenServiceImpl {

    private final AuthenticationsImpl authenticationsImpl;
    private static final String REFRESHTOKEN_GRANTTYPE = "refresh_token";
    private static final String ACCESSTOKEN_GRANTTYPE = "access_token";

    /**
     * Creates an instance of the token service impl class.TokenServiceImpl.java
     *
     * @param url               the service endpoint.
     * @param apiVersion        the api-version of the service being targeted.
     * @param pipeline          the pipeline to use to make the call.
     * @param serializerAdapter the serializer adapter for the rest client.
     */
    public TokenServiceImpl(String url, ContainerRegistryServiceVersion apiVersion, HttpPipeline pipeline, SerializerAdapter serializerAdapter) {
        if (serializerAdapter == null) {
            serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        }

        if (apiVersion == null) {
            apiVersion = ContainerRegistryServiceVersion.getLatest();
        }

        this.authenticationsImpl = new AuthenticationsImpl(url, apiVersion.getVersion(), pipeline, serializerAdapter);
    }

    /**
     * Gets the ACR access token.
     *
     * @param acrRefreshToken Given the ACRs refresh token.
     * @param scope           - Token scope.
     * @param serviceName     The name of the service.
     */
    public Mono<AccessToken> getAcrAccessTokenAsync(String acrRefreshToken, String scope, String serviceName, TokenGrantType grantType) {
        return withContext(context -> this.authenticationsImpl.exchangeAcrRefreshTokenForAcrAccessTokenWithResponseAsync(serviceName, scope, acrRefreshToken, grantType, context)
            .flatMap(response -> {
                AcrAccessToken token = response.getValue();
                if (token != null) {
                    String accessToken = token.getAccessToken();
                    OffsetDateTime expirationTime = JsonWebToken.retrieveExpiration(accessToken);
                    return Mono.just(new AccessToken(accessToken, expirationTime));
                }

                return Mono.empty();
            }));
    }

    /**
     * Gets an ACR refresh token.
     *
     * @param aadAccessToken Given the ACR access token.
     * @param serviceName    Given the ACR service.
     */
    public Mono<AccessToken> getAcrRefreshTokenAsync(String aadAccessToken, String serviceName) {
        return withContext(context -> this.authenticationsImpl.exchangeAadAccessTokenForAcrRefreshTokenWithResponseAsync(PostContentSchemaGrantType.ACCESS_TOKEN, serviceName, null, null, aadAccessToken, context).flatMap(response -> {
            AcrRefreshToken token = response.getValue();
            if (token != null) {
                String refreshToken = token.getRefreshToken();
                OffsetDateTime expirationTime = JsonWebToken.retrieveExpiration(refreshToken);
                return Mono.just(new AccessToken(refreshToken, expirationTime));
            }

            return Mono.empty();
        }));
    }
}
