// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Common test utilities.
 */
public final class TestUtils {
    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TestUtils() {
    }

    public static class PerCallPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().setHeader("Custom-Header", "Some Value");

            return next.process();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    public static class PerRetryPolicy implements HttpPipelinePolicy {
        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().setHeader("Custom-Header", "Some Value");

            return next.process();
        }
    }

    public static class TestCredential implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext request) {
            return Mono.just(new AccessToken("TestAccessToken", OffsetDateTime.now().plusHours(1)));
        }
    }

    public static HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }

    public static HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .build();
    }
}
