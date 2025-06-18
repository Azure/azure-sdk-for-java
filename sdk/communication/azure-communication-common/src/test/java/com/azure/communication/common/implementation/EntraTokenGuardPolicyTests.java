// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common.implementation;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.communication.common.EntraCredentialHelper.*;
import static org.junit.jupiter.api.Assertions.*;

public class EntraTokenGuardPolicyTests {

    private HttpRequest request;
    private AtomicInteger callCount;

    @BeforeEach
    void setUp() {
        request = new HttpRequest(HttpMethod.POST, "https://endpoint");
        request.setHeader(HttpHeaderName.AUTHORIZATION, AUTH_HEADER);
        callCount = new AtomicInteger();
    }

    private HttpPipeline buildPipeline(HttpPipelinePolicy countingPolicy) {
        return new HttpPipelineBuilder().policies(new EntraTokenGuardPolicy(), countingPolicy).build();
    }

    @Test
    void processFirstCallWithNoCachedTokenInvokesNextProcess() {
        HttpPipelinePolicy countingPolicy = (context, next) -> {
            callCount.incrementAndGet();
            return Mono.just(new MockHttpResponse(context.getHttpRequest(), 200,
                VALID_TOKEN_RESPONSE.getBytes(StandardCharsets.UTF_8)));
        };
        HttpPipeline pipeline = buildPipeline(countingPolicy);

        pipeline.send(request).block();

        assertEquals(1, callCount.get());
    }

    @Test
    void processSecondCallWithDifferentTokenInvokesNextProcessAgain() {
        HttpPipelinePolicy countingPolicy = (context, next) -> {
            callCount.incrementAndGet();
            return Mono.just(new MockHttpResponse(context.getHttpRequest(), 200,
                VALID_TOKEN_RESPONSE.getBytes(StandardCharsets.UTF_8)));
        };
        HttpPipeline pipeline = buildPipeline(countingPolicy);

        pipeline.send(request).block();
        request.setHeader(HttpHeaderName.AUTHORIZATION, ALT_AUTH_HEADER);
        pipeline.send(request).block();

        assertEquals(2, callCount.get());
    }

    @Test
    void processWithExpiredAcsTokenInResponseInvokesNextProcessAgain() {
        String expiredToken = new JwtTokenMocker().generateRawToken("resource", "user",
            OffsetDateTime.now().minusMinutes(1).toInstant());
        String expiredTokenResponse
            = String.format(TOKEN_RESPONSE_TEMPLATE, expiredToken, OffsetDateTime.now().minusMinutes(1));

        HttpPipelinePolicy countingPolicy = (context, next) -> {
            callCount.incrementAndGet();
            return Mono.just(new MockHttpResponse(context.getHttpRequest(), 200,
                expiredTokenResponse.getBytes(StandardCharsets.UTF_8)));
        };
        HttpPipeline pipeline = buildPipeline(countingPolicy);

        pipeline.send(request).block();
        pipeline.send(request).block();

        assertEquals(2, callCount.get());
    }

    @Test
    void processPreviousExchangeCallFailedInvokesNextProcessAgain() {
        HttpPipelinePolicy countingPolicy = (context, next) -> {
            callCount.incrementAndGet();
            return Mono.just(new MockHttpResponse(context.getHttpRequest(), 400));
        };
        HttpPipeline pipeline = buildPipeline(countingPolicy);

        pipeline.send(request).block();
        pipeline.send(request).block();

        assertEquals(2, callCount.get());
    }

    @Test
    void processWithInvalidResponseBodyInvokesNextProcessAgain() {
        HttpPipelinePolicy countingPolicy = (context, next) -> {
            callCount.incrementAndGet();
            return Mono
                .just(new MockHttpResponse(context.getHttpRequest(), 200, "not-json".getBytes(StandardCharsets.UTF_8)));
        };
        HttpPipeline pipeline = buildPipeline(countingPolicy);

        pipeline.send(request).block();
        pipeline.send(request).block();

        assertEquals(2, callCount.get());
    }

    @Test
    void processSecondCallWithSameTokenAndValidAcsTokenUsesCacheAndReturnsOriginalResponse() {
        AtomicReference<MockHttpResponse> responseRef = new AtomicReference<>(
            new MockHttpResponse(request, 200, VALID_TOKEN_RESPONSE.getBytes(StandardCharsets.UTF_8)));

        HttpPipelinePolicy countingPolicy = (context, next) -> {
            callCount.incrementAndGet();
            return Mono.just(responseRef.get());
        };
        HttpPipeline pipeline = buildPipeline(countingPolicy);

        HttpResponse firstResponse = pipeline.send(request).block();

        // Change the response to 400
        responseRef.set(new MockHttpResponse(request, 400));

        // Second call: should return cached 200 response, not the new 400
        HttpResponse secondResponse = pipeline.send(request).block();

        assertEquals(1, callCount.get());
        assertSame(firstResponse, secondResponse);
    }
}
