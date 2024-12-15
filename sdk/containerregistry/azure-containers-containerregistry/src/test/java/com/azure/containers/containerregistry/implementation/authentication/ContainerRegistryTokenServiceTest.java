// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.containers.containerregistry.ContainerRegistryServiceVersion;
import com.azure.containers.containerregistry.implementation.AuthenticationsImpl;
import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.models.AcrAccessToken;
import com.azure.containers.containerregistry.implementation.models.AcrRefreshToken;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainerRegistryTokenServiceTest {
    @Test
    public void refreshTokenRestAPICalledOnlyOnce() {
        String mockToken = "{\"exp\":" + OffsetDateTime.now().plusHours(1).toEpochSecond() + "}";
        String mockBase64 = "." + Base64.getEncoder().encodeToString(mockToken.getBytes(StandardCharsets.UTF_8)) + ".";
        AcrRefreshToken refreshToken = new AcrRefreshToken().setRefreshToken(mockBase64);
        AcrAccessToken accessToken = new AcrAccessToken().setAccessToken(mockBase64);

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            String path = request.getUrl().getPath();
            if (request.getUrl().getPath().contains("/oauth2/token")) {
                return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(), accessToken));
            } else if (path.contains("/oauth2/exchange")) {
                return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(), refreshToken));
            } else {
                return null;
            }
        }).build();
        AuthenticationsImpl authenticationsImpl = new AzureContainerRegistryImpl(pipeline, "https://mytest.azurecr.io",
            ContainerRegistryServiceVersion.getLatest().toString()).getAuthentications();
        AtomicInteger callCount = new AtomicInteger();
        TokenCredential refreshTokenCredential = tokenRequestContext -> {
            int count = callCount.getAndIncrement();
            if (count == 0) {
                return Mono.just(new AccessToken(accessToken.getAccessToken(), OffsetDateTime.now().plusHours(1)));
            } else if (count == 1) {
                return Mono.just(new AccessToken(refreshToken.getRefreshToken(), OffsetDateTime.now().plusHours(1)));
            } else {
                return null;
            }
        };

        ContainerRegistryTokenService service
            = new ContainerRegistryTokenService(authenticationsImpl, new AccessTokenCacheImpl(refreshTokenCredential));

        int count = 10;
        StepVerifier.create(Flux.range(1, count)
            .flatMap(i -> service.getToken(new ContainerRegistryTokenRequestContext("serviceName", "scope")))
            .subscribeOn(Schedulers.newParallel("pool", count))).expectNextCount(count).verifyComplete();

        // We call the refreshToken method only once.
        assertEquals(1, callCount.get());
    }
}
