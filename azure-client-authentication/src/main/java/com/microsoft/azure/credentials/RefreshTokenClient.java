/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.interceptors.LoggingPolicy;
import com.microsoft.rest.serializer.JacksonAdapter;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

import java.net.Proxy;
import java.util.Date;

/**
 * This class encloses a Retrofit client that refreshes a token from ADAL.
 */
@Beta
final class RefreshTokenClient {
    private final RefreshTokenService service;

    RefreshTokenClient(String baseUrl, Proxy proxy) {
        // FIXME
        OkHttpClient.Builder builder = new OkHttpClient.Builder();//.addInterceptor(new LoggingPolicy(LogLevel.BODY_AND_HEADERS));
        if (proxy != null) {
            builder = builder.proxy(proxy);
        }
        service = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(new JacksonAdapter().converterFactory())
                .baseUrl(baseUrl)
                .client(builder.build())
            .build().create(RefreshTokenService.class);
    }

    AuthenticationResult refreshToken(String tenant, String clientId, String resource, String refreshToken, boolean isMultipleResourceRefreshToken) {
        try {
            RefreshTokenResult result = service.refreshToken(tenant, clientId, "refresh_token", resource, refreshToken)
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
        @FormUrlEncoded
        @POST("{tenant}/oauth2/token")
        Observable<RefreshTokenResult> refreshToken(
            @Path("tenant") String tenant,
            @Field("client_id") String clientId,
            @Field("grant_type") String grantType,
            @Field("resource") String resource,
            @Field("refresh_token") String refreshToken);
    }

    private static class RefreshTokenResult {
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
