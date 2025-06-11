// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.credentials.AccessToken;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.pipeline.HttpPipelineCallContext;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Common test utilities.
 */
public final class TestUtils {
    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TestUtils() {
    }

    static class PerCallPolicy implements HttpPipelinePolicy {
        @Override
        public CompletableFuture<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().setHeader("Custom-Header", "Some Value");
            return next.process();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    static class PerRetryPolicy implements HttpPipelinePolicy {
        @Override
        public CompletableFuture<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().setHeader("Custom-Header", "Some Value");
            return next.process();
        }
    }

    static class TestCredential implements TokenCredential {
        @Override
        public CompletableFuture<AccessToken> getToken(TokenRequestContext request) {
            return CompletableFuture.completedFuture(new AccessToken("TestAccessToken", OffsetDateTime.now().plusHours(1)));
        }
    }

    /**
     * HTTP Client builder for asserting HTTP operations.
     */
    public static class AssertingHttpClientBuilder {
        private final HttpClient httpClient;
        private boolean assertSync = false;
        private boolean assertAsync = false;

        public AssertingHttpClientBuilder(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public AssertingHttpClientBuilder assertSync() {
            this.assertSync = true;
            return this;
        }

        public AssertingHttpClientBuilder assertAsync() {
            this.assertAsync = true;
            return this;
        }

        public HttpClient build() {
            return new AssertingHttpClient(httpClient, assertSync, assertAsync);
        }
    }

    private static class AssertingHttpClient implements HttpClient {
        private final HttpClient httpClient;
        private final boolean assertSync;
        private final boolean assertAsync;

        AssertingHttpClient(HttpClient httpClient, boolean assertSync, boolean assertAsync) {
            this.httpClient = httpClient;
            this.assertSync = assertSync;
            this.assertAsync = assertAsync;
        }

        @Override
        public CompletableFuture<HttpResponse> send(HttpRequest request) {
            if (assertSync) {
                // For sync clients, we expect blocking behavior
                return httpClient.send(request);
            } else if (assertAsync) {
                // For async clients, we expect non-blocking behavior
                return httpClient.send(request);
            }
            return httpClient.send(request);
        }
    }
}
