// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Token service implementation that wraps the authentication rest APIs for ACR.
 */
public class TokenServiceImpl {

    private final AuthenticationsImpl authenticationsImpl;
    private static final String REFRESHTOKEN_GRANTTYPE = "refresh_token";
    private static final String ACCESSTOKEN_GRANTTYPE = "access_token";

    /**
     * Creates an instance of the token service impl class.TokenServiceImpl.java
     *  @param url the service endpoint.
     * @param pipeline the pipeline to use to make the call.
     * @param serializerAdapter the serializer adapter for the rest client.
     *
     */
    public TokenServiceImpl(String url, HttpPipeline pipeline, SerializerAdapter serializerAdapter) {
        if (serializerAdapter == null) {
            serializerAdapter = JacksonAdapter.createDefaultSerializerAdapter();
        }

        this.authenticationsImpl = new AuthenticationsImpl(url, pipeline, serializerAdapter);
    }

    /**
     * Gets the ACR access token.
     * @param acrRefreshToken Given the ACRs refresh token.
     * @param scope - Token scope.
     * @param serviceName The name of the service.
     *
     */
    public Mono<AccessToken> getAcrAccessTokenAsync(String acrRefreshToken, String scope, String serviceName, String grantType) {
        return this.authenticationsImpl.exchangeAcrRefreshTokenForAcrAccessTokenAsync(serviceName, scope, grantType, acrRefreshToken)
            .map(token -> {
                String accessToken = token.getAccessToken();
                OffsetDateTime expirationTime = JsonWebToken.retrieveExpiration(accessToken);
                return new AccessToken(accessToken, expirationTime);
            });
    }

    /**
     * Gets an ACR refresh token.
     * @param aadAccessToken Given the ACR access token.
     * @param serviceName Given the ACR service.
     *
     */
    public Mono<AccessToken> getAcrRefreshTokenAsync(String aadAccessToken, String serviceName) {
        return this.authenticationsImpl.exchangeAadAccessTokenForAcrRefreshTokenAsync(
            serviceName,
            aadAccessToken).map(token -> {
                String refreshToken = token.getRefreshToken();
                OffsetDateTime expirationTime = JsonWebToken.retrieveExpiration(refreshToken);
                return new AccessToken(refreshToken, expirationTime);
            });
    }
}
