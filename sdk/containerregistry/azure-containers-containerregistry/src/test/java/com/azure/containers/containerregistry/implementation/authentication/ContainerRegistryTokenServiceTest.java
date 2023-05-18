// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.implementation.AuthenticationsImpl;
import com.azure.containers.containerregistry.implementation.models.AcrAccessToken;
import com.azure.containers.containerregistry.implementation.models.AcrRefreshToken;
import com.azure.containers.containerregistry.implementation.models.PostContentSchemaGrantType;
import com.azure.containers.containerregistry.implementation.models.TokenGrantType;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContainerRegistryTokenServiceTest {

    private HttpPipeline httpPipeline = mock(HttpPipeline.class);
    private AuthenticationsImpl authenticationsImpl = mock(AuthenticationsImpl.class);
    private AccessTokenCacheImpl refreshTokenCache;
    private TokenCredential refreshTokenCredential = mock(TokenCredential.class);
    private ContainerRegistryTokenRequestContext requestContext = mock(ContainerRegistryTokenRequestContext.class);

    private static final String SCOPE = "scope";
    private static final String SERVICENAME = "serviceName";
    private static final String REFRESHTOKEN = "refresh_token";
    private static final String ACCESSTOKEN = "access_token";

    @BeforeEach
    public void setup() {
        AcrRefreshToken refreshToken = new AcrRefreshToken().setRefreshToken(REFRESHTOKEN);
        AcrAccessToken accessToken = new AcrAccessToken().setAccessToken(ACCESSTOKEN);
        Response<AcrAccessToken> accessTokenResponse = new SimpleResponse<>(null, 200, new HttpHeaders(), accessToken);
        Response<AcrRefreshToken> refreshTokenResponse = new SimpleResponse<>(null, 200, new HttpHeaders(), refreshToken);

        when(authenticationsImpl.exchangeAcrRefreshTokenForAcrAccessTokenWithResponseAsync(anyString(), anyString(), anyString(), any(TokenGrantType.class), any(Context.class)))
            .thenReturn(Mono.just(accessTokenResponse));

        when(authenticationsImpl.exchangeAadAccessTokenForAcrRefreshTokenWithResponseAsync(any(PostContentSchemaGrantType.class), anyString(), anyString(), anyString(), anyString(), any(Context.class)))
            .thenReturn(Mono.just(refreshTokenResponse));
        when(refreshTokenCredential.getToken(any(TokenRequestContext.class))).thenReturn(Mono.just(new AccessToken(accessToken.getAccessToken(), OffsetDateTime.now().plusHours(1))));
        when(requestContext.getScope()).thenReturn(SCOPE);
        when(requestContext.getServiceName()).thenReturn(SERVICENAME);
        when(refreshTokenCredential.getToken(any())).thenReturn(Mono.just(new AccessToken(REFRESHTOKEN, OffsetDateTime.now().plusHours(1))));

        refreshTokenCache = new AccessTokenCacheImpl(refreshTokenCredential);
    }

    @Test
    public void refreshTokenRestAPICalledOnlyOnce() {
        ContainerRegistryTokenService service = new ContainerRegistryTokenService(authenticationsImpl, refreshTokenCache);

        int count = 10;
        StepVerifier.create(Flux.range(1, count)
            .flatMap(i -> service.getToken(requestContext))
            .subscribeOn(Schedulers.newParallel("pool", count)))
            .expectNextCount(count)
            .verifyComplete();

        // We call the refreshToken method only once.
        verify(this.refreshTokenCredential, times(1)).getToken(this.requestContext);

    }
}
