/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.HostParam;
import com.microsoft.rest.v2.http.ContentType;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.RestProxy;
import com.microsoft.rest.v2.annotations.BodyParam;
import com.microsoft.rest.v2.annotations.ExpectedResponses;
import com.microsoft.rest.v2.annotations.POST;
import com.microsoft.rest.v2.annotations.PathParam;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.NettyClient;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import java.net.Proxy;
import java.util.Date;

/**
 * This class encloses a Retrofit client that refreshes a token from ADAL.
 */
@Beta
final class RefreshTokenClient {
    private final RefreshTokenService service;
    private final String baseUrl;

    RefreshTokenClient(String baseUrl, Proxy proxy) {
        this(baseUrl, createHttpClient(proxy));
    }

    RefreshTokenClient(String baseUrl, HttpClient httpClient) {
        service = RestProxy.create(RefreshTokenService.class, HttpPipeline.build(httpClient));
        this.baseUrl = baseUrl;
    }

    private static HttpClient createHttpClient(Proxy proxy) {
        return new NettyClient.Factory()
                .create(new HttpClient.Configuration(proxy));
    }

    AuthenticationResult refreshToken(String tenant, String clientId, String resource, String refreshToken, boolean isMultipleResoureRefreshToken) {
        AuthenticationResult result;
        try {
            result = refreshTokenAsync(tenant, clientId, resource, refreshToken, isMultipleResoureRefreshToken).blockingGet();
        }
        catch (Exception ignored) {
            result = null;
        }
        return result;
    }

    Single<AuthenticationResult> refreshTokenAsync(String tenant, String clientId, String resource, String refreshToken, final boolean isMultipleResourceRefreshToken) {
        final Escaper escaper = UrlEscapers.urlFormParameterEscaper();
        final String bodyString = String.format(
                "client_id=%s&grant_type=%s&resource=%s&refresh_token=%s",
                escaper.escape(clientId),
                "refresh_token",
                escaper.escape(resource),
                escaper.escape(refreshToken));

        return service.refreshToken(baseUrl, tenant, bodyString)
                .map(new Function<RefreshTokenResponse, AuthenticationResult>() {
                    @Override
                    public AuthenticationResult apply(RefreshTokenResponse refreshTokenResponse) {
                        AuthenticationResult result = null;
                        if (refreshTokenResponse != null) {
                            result = new AuthenticationResult(
                                    refreshTokenResponse.tokenType,
                                    refreshTokenResponse.accessToken,
                                    refreshTokenResponse.refreshToken,
                                    refreshTokenResponse.expiresIn,
                                    null,
                                    null,
                                    isMultipleResourceRefreshToken);
                        }
                        return result;
                    }
                });
    }

    @Host("{baseUrl}")
    private interface RefreshTokenService {
        @POST("{tenant}/oauth2/token")
        @ExpectedResponses(200)
        Single<RefreshTokenResponse> refreshToken(@HostParam("baseUrl") String baseUrl, @PathParam("tenant") String tenant, @BodyParam(ContentType.APPLICATION_OCTET_STREAM) String requestBody);
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
