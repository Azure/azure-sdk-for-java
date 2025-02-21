// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class StorageBearerTokenChallengeAuthorizationPolicyTests {

    private String[] scopes;
    private TokenCredential mockCredential;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        scopes = new String[]{"https://storage.azure.com/.default"};
        mockCredential = mock(TokenCredential.class);
    }

    @Test
    public void usesTokenProvidedByCredentials() {
        StorageBearerTokenChallengeAuthorizationPolicy policy = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, scopes);

        HttpPipelineCallContext mockContext = mock(HttpPipelineCallContext.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE)).thenReturn(null);

        Mono<Boolean> result = policy.authorizeRequestOnChallenge(mockContext, mockResponse);

        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void doesNotSendUnauthorizedRequestWhenEnableTenantDiscoveryIsFalse() {
        StorageBearerTokenChallengeAuthorizationPolicy policy = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, scopes);

        HttpPipelineCallContext mockContext = mock(HttpPipelineCallContext.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE)).thenReturn(null);

        for (int i = 0; i < 10; i++) {
            Mono<Boolean> result = policy.authorizeRequestOnChallenge(mockContext, mockResponse);
            StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
        }
    }

    @Test
    public void sendsUnauthorizedRequestWhenEnableTenantDiscoveryIsTrue() {
        StorageBearerTokenChallengeAuthorizationPolicy realPolicy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, scopes);

        // Spy on the real instance
        StorageBearerTokenChallengeAuthorizationPolicy policy = spy(realPolicy);

        String expectedTenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";

        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "https://example.com");
        HttpPipelineCallContext mockContext = mock(HttpPipelineCallContext.class);
        when(mockContext.getHttpRequest()).thenReturn(httpRequest);

        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE)).thenReturn(
            "Bearer authorization_uri=https://login.microsoftonline.com/" + expectedTenantId + "/oauth2/authorize resource_id=https://storage.azure.com");

        // Properly stub the method on the spy
        doReturn(Mono.empty()).when(policy).setAuthorizationHeader(any(), any());

        for (int i = 0; i < 10; i++) {
            Mono<Boolean> result = policy.authorizeRequestOnChallenge(mockContext, mockResponse);

            StepVerifier.create(result)
                .expectNext(true)  // Expect the Mono<Boolean> to emit 'true'
                .verifyComplete();
        }
    }


    @Test
    public void usesScopeFromBearerChallenge() {
        StorageBearerTokenChallengeAuthorizationPolicy realPolicy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://disk.compute.azure.com/.default");

        // Spy on the real instance to allow stubbing setAuthorizationHeader
        StorageBearerTokenChallengeAuthorizationPolicy policy = spy(realPolicy);

        String serviceChallengeResponseScope = "https://storage.azure.com";

        HttpPipelineCallContext mockContext = mock(HttpPipelineCallContext.class);
        HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE)).thenReturn(
            "Bearer authorization_uri=https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize resource_id=" + serviceChallengeResponseScope);

        // Stub the setAuthorizationHeader method so it returns a completed Mono
        doReturn(Mono.empty()).when(policy).setAuthorizationHeader(any(), any());

        for (int i = 0; i < 2; i++) {
            Mono<Boolean> result = policy.authorizeRequestOnChallenge(mockContext, mockResponse);

            StepVerifier.create(result)
                .expectNext(true)  // Expect 'true' instead of failing
                .verifyComplete();
        }
    }

}
