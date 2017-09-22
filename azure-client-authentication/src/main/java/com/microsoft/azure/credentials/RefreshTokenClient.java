/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.RestProxy;
import com.microsoft.rest.annotations.BodyParam;
import com.microsoft.rest.annotations.POST;
import com.microsoft.rest.annotations.PathParam;
import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.serializer.JacksonAdapter;
import rx.Observable;
import rx.Single;

import java.net.Proxy;
import java.util.Date;

/**
 * This class encloses a Retrofit client that refreshes a token from ADAL.
 */
@Beta
final class RefreshTokenClient {
    private final RefreshTokenService service;

    RefreshTokenClient(String baseUrl, Proxy proxy) {
        // FIXME: strip out OkHttp and Retrofit
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (proxy != null) {
            builder = builder.proxy(proxy);
        }

        final HttpClient httpClient = RestClient.createDefaultHttpClient();
        httpClient.


        service = RestProxy.create(RefreshTokenService.class, baseUrl, RestClient.DEFAULT_HTTP_CLIENT, RestClient.DEFAULT_SERIALIZER);
    }

    AuthenticationResult refreshToken(String tenant, String clientId, String resource, String refreshToken, boolean isMultipleResoureRefreshToken) {
        return null;
    }

    Single<AuthenticationResult> refreshTokenAsync(String tenant, String clientId, String resource, String refreshToken, boolean isMultipleResourceRefreshToken) {

    }

    AuthenticationResult refreshTokenOriginal(String tenant, String clientId, String resource, String refreshToken, boolean isMultipleResourceRefreshToken) {
        try {
            RefreshTokenResponse result = service.refreshToken(tenant, clientId, "refresh_token", resource, refreshToken)
                .toBlocking().single();
            if (result == null) {
                return null;
            }
            return new AuthenticationResult(
                result.tokenType,
                result.accessToken,
                result.refreshToken,
                result.expiresIn,
                null,
                null,
                isMultipleResourceRefreshToken);
        } catch (Exception e) {
            return null;
        }
    }

    private interface RefreshTokenService {
        @POST("{tenant}/oauth2/token")
        Observable<RefreshTokenResponse> refreshToken(
            @PathParam("tenant") String tenant,
            @BodyParam String requestBody;
            @Field("client_id") String clientId,
            @Field("grant_type") String grantType,
            @Field("resource") String resource,
            @Field("refresh_token") String refreshToken);
    }

    private static class RefreshTokenResponse {
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("expires_in")
        private long expiresIn;
        @JsonProperty("expires_on")
        private Date expiresOn;
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("refresh_token")
        private String refreshToken;
    }
}
