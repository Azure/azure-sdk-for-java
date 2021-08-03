// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ContainerRegistryTokenServiceTest {

    private TokenCredential tokenCredential;
    private HttpPipeline httpPipeline;
    private SerializerAdapter serializerAdapter;
    private TokenServiceImpl tokenServiceImpl;
    private AccessTokenCacheImpl refreshTokenCache;
    private ContainerRegistryTokenRequestContext requestContext;
    private ContainerRegistryRefreshTokenCredential refreshTokenCredential;

    private static final String SCOPE = "scope";
    private static final String SERVICENAME = "serviceName";
    private static final String REFRESHTOKEN = "refresh_token";
    private static final String ACCESSTOKEN = "access_token";


    @BeforeEach
    public void setup() {
        this.httpPipeline = mock(HttpPipeline.class);
        this.serializerAdapter = mock(SerializerAdapter.class);

        TokenServiceImpl impl = mock(TokenServiceImpl.class);
        AccessToken refreshToken = new AccessToken(REFRESHTOKEN, OffsetDateTime.now().plusMinutes(30));
        AccessToken accessToken = new AccessToken(ACCESSTOKEN, OffsetDateTime.now().plusMinutes(30));
        when(impl.getAcrAccessTokenAsync(anyString(), anyString(), anyString(), anyString())).thenReturn(Mono.just(accessToken));
        when(impl.getAcrRefreshTokenAsync(anyString(), anyString())).thenReturn(Mono.just(refreshToken));

        TokenCredential tokenCredential = mock(TokenCredential.class);
        when(tokenCredential.getToken(any(TokenRequestContext.class))).thenReturn(Mono.just(accessToken));

        ContainerRegistryTokenRequestContext tokenRequestContext = mock(ContainerRegistryTokenRequestContext.class);
        when(tokenRequestContext.getScope()).thenReturn(SCOPE);
        when(tokenRequestContext.getServiceName()).thenReturn(SERVICENAME);

        ContainerRegistryRefreshTokenCredential spyRefreshTokenCredential = spy(mock(ContainerRegistryRefreshTokenCredential.class));
        doReturn(Mono.just(refreshToken)).when(spyRefreshTokenCredential).getToken(any(ContainerRegistryTokenRequestContext.class));



        AccessTokenCacheImpl refreshTokenCache = new AccessTokenCacheImpl(spyRefreshTokenCredential);
        this.tokenCredential = tokenCredential;
        this.refreshTokenCache = refreshTokenCache;
        this.refreshTokenCredential = spyRefreshTokenCredential;
        this.requestContext = tokenRequestContext;
        this.tokenServiceImpl = impl;
    }

    @Test
    public void refreshTokenRestAPICalledOnlyOnce() throws Exception {
        ContainerRegistryTokenService service = new ContainerRegistryTokenService(
            this.tokenCredential,
            null,
            "myString",
            this.httpPipeline,
            this.serializerAdapter
        );

        service.setTokenService(this.tokenServiceImpl);
        service.setRefreshTokenCache(this.refreshTokenCache);

        CountDownLatch latch = new CountDownLatch(1);

        Flux.range(1, 10)
            .flatMap(i -> Mono.just(OffsetDateTime.now())
                // Runs cache.getToken() on 10 different threads
                .subscribeOn(Schedulers.newParallel("pool", 10))
                .flatMap(
                    start -> service.getToken(this.requestContext).map(accessToken -> 1))
                )
                .doOnComplete(latch::countDown)
                .subscribe();

        latch.await();

        // We call the acrrefreshToken method only once.
        verify(this.refreshTokenCredential, times(1)).getToken(this.requestContext);

    }
}
