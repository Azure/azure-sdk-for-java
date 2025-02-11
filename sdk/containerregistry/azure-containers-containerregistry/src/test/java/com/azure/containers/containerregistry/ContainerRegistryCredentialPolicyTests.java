// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.containers.containerregistry.implementation.AzureContainerRegistryImpl;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryCredentialsPolicy;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenRequestContext;
import com.azure.containers.containerregistry.implementation.authentication.ContainerRegistryTokenService;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerRegistryCredentialPolicyTests {

    public static final String AUTHENTICATE_HEADER
        = "Bearer realm=\"https://mytest.azurecr.io/oauth2/token\",service=\"mytest.azurecr.io\",scope=\"registry:catalog:*\",error=\"invalid_token\"";
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer SUCCESS = 200;
    public static final String BEARER = "Bearer";
    public static final String SERVICENAME = "mytest.azurecr.io";
    public static final String SCOPENAME = "registry:catalog:*";
    private static final HttpRequest REQUEST = new HttpRequest(HttpMethod.GET, "https://mytest.azurecr.io");

    private ContainerRegistryTokenService service;
    private HttpResponse unauthorizedHttpResponse;
    private HttpResponse unauthorizedHttpResponseWithoutHeader;
    private HttpPipelineCallContext callContext;
    private HttpResponse successResponse;

    @BeforeEach
    public void setup() {
        AccessToken accessToken = new AccessToken("tokenValue", OffsetDateTime.now().plusMinutes(30));

        ContainerRegistryTokenService mockService
            = new ContainerRegistryTokenService(null, null, new AzureContainerRegistryImpl("https://azure.com",
                ContainerRegistryServiceVersion.getLatest().toString())) {
                @Override
                public Mono<AccessToken> getToken(TokenRequestContext request) {
                    return Mono.just(accessToken);
                }

                @Override
                public AccessToken getTokenSync(TokenRequestContext tokenRequestContext) {
                    return accessToken;
                }
            };

        AtomicReference<HttpPipelineCallContext> contextReference = new AtomicReference<>();
        new HttpPipelineBuilder().policies((httpPipelineCallContext, httpPipelineNextPolicy) -> {
            contextReference.set(httpPipelineCallContext);
            return Mono.empty();
        }).httpClient(ignored -> Mono.empty()).build().sendSync(REQUEST, Context.NONE);

        MockHttpResponse unauthorizedResponseWithHeader = new MockHttpResponse(REQUEST, UNAUTHORIZED,
            new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE, AUTHENTICATE_HEADER));

        MockHttpResponse unauthorizedResponseWithoutHeader = new MockHttpResponse(REQUEST, UNAUTHORIZED);

        MockHttpResponse successResponse = new MockHttpResponse(REQUEST, SUCCESS);

        this.service = mockService;
        this.unauthorizedHttpResponse = unauthorizedResponseWithHeader;
        this.unauthorizedHttpResponseWithoutHeader = unauthorizedResponseWithoutHeader;
        this.callContext = contextReference.get();
        this.successResponse = successResponse;
    }

    @SyncAsyncTest
    public void requestNoRetryOnOtherErrorCodes() {
        AtomicInteger syncCallCount = new AtomicInteger();
        AtomicInteger asyncCallCount = new AtomicInteger();
        ContainerRegistryCredentialsPolicy policy = new ContainerRegistryCredentialsPolicy(this.service, "foo") {
            @Override
            public Mono<Void> setAuthorizationHeader(HttpPipelineCallContext context,
                TokenRequestContext tokenRequestContext) {
                asyncCallCount.incrementAndGet();
                return super.setAuthorizationHeader(context, tokenRequestContext);
            }

            @Override
            public void setAuthorizationHeaderSync(HttpPipelineCallContext context,
                TokenRequestContext tokenRequestContext) {
                syncCallCount.incrementAndGet();
                super.setAuthorizationHeaderSync(context, tokenRequestContext);
            }
        };

        HttpPipeline pipeline
            = new HttpPipelineBuilder().policies(policy).httpClient(request -> Mono.just(successResponse)).build();

        SyncAsyncExtension.execute(() -> pipeline.sendSync(REQUEST, Context.NONE), () -> pipeline.send(REQUEST));

        // Make sure no call being done to the authorize request.
        assertEquals(0, asyncCallCount.get());
        assertEquals(0, syncCallCount.get());

        HttpPipeline pipeline2 = new HttpPipelineBuilder().policies(policy)
            .httpClient(request -> Mono.just(unauthorizedHttpResponseWithoutHeader))
            .build();

        SyncAsyncExtension.execute(() -> pipeline2.sendSync(REQUEST, Context.NONE), () -> pipeline2.send(REQUEST));

        // Make sure no call being done to the authorize request.
        assertEquals(0, asyncCallCount.get());
        assertEquals(0, syncCallCount.get());
    }

    @Test
    public void requestAddBearerTokenToRequest() {
        AtomicReference<TokenRequestContext> contextReference = new AtomicReference<>();
        AtomicInteger callCount = new AtomicInteger();
        ContainerRegistryCredentialsPolicy policy = new ContainerRegistryCredentialsPolicy(this.service, "foo") {
            @Override
            public Mono<Void> setAuthorizationHeader(HttpPipelineCallContext context,
                TokenRequestContext tokenRequestContext) {
                callCount.getAndIncrement();
                contextReference.set(tokenRequestContext);
                return super.setAuthorizationHeader(context, tokenRequestContext);
            }
        };

        // Validate that the onChallenge ran successfully.
        StepVerifier.create(policy.authorizeRequestOnChallenge(this.callContext, this.unauthorizedHttpResponse))
            .assertNext(Assertions::assertTrue)
            .verifyComplete();

        String tokenValue = this.callContext.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));

        assertEquals(1, callCount.get());
        ContainerRegistryTokenRequestContext requestContext
            = assertInstanceOf(ContainerRegistryTokenRequestContext.class, contextReference.get());
        assertEquals(SERVICENAME, requestContext.getServiceName());
        assertEquals(SCOPENAME, requestContext.getScopes().get(0));
    }

    @Test
    public void requestAddBearerTokenToRequestSync() {
        AtomicReference<TokenRequestContext> contextReference = new AtomicReference<>();
        AtomicInteger callCount = new AtomicInteger();
        ContainerRegistryCredentialsPolicy policy = new ContainerRegistryCredentialsPolicy(this.service, "foo") {
            @Override
            public void setAuthorizationHeaderSync(HttpPipelineCallContext context,
                TokenRequestContext tokenRequestContext) {
                callCount.getAndIncrement();
                contextReference.set(tokenRequestContext);
                super.setAuthorizationHeaderSync(context, tokenRequestContext);
            }
        };

        boolean onChallenge = policy.authorizeRequestOnChallengeSync(this.callContext, this.unauthorizedHttpResponse);

        // Validate that the onChallenge ran successfully.
        assertTrue(onChallenge);

        String tokenValue = this.callContext.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertFalse(tokenValue.isEmpty());
        assertTrue(tokenValue.startsWith(BEARER));

        assertEquals(1, callCount.get());
        ContainerRegistryTokenRequestContext requestContext
            = assertInstanceOf(ContainerRegistryTokenRequestContext.class, contextReference.get());
        assertEquals(SERVICENAME, requestContext.getServiceName());
        assertEquals(SCOPENAME, requestContext.getScopes().get(0));
    }
}
