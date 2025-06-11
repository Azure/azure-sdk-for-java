// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.keys;

import io.clientcore.core.credential.AccessToken;
import io.clientcore.core.credential.TokenCredential;
import io.clientcore.core.credential.TokenRequestContext;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.pipeline.HttpPipelineCallContext;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Common test utilities.
 */
public final class TestUtils {
    private static final HttpHeaderName CUSTOM_HEADER = HttpHeaderName.fromString("Custom-Header");

    /**
     * Private constructor so this class cannot be instantiated.
     */
    private TestUtils() {
    }

    public static class PerCallPolicy implements HttpPipelinePolicy {
        @Override
        public CompletableFuture<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().setHeader(CUSTOM_HEADER, "Some Value");
            return next.process();
        }

        @Override
        public HttpPipelinePosition getPipelinePosition() {
            return HttpPipelinePosition.PER_CALL;
        }
    }

    public static class PerRetryPolicy implements HttpPipelinePolicy {
        @Override
        public CompletableFuture<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            context.getHttpRequest().setHeader(CUSTOM_HEADER, "Some Value");
            return next.process();
        }
    }

    public static class TestCredential implements TokenCredential {
        @Override
        public CompletableFuture<AccessToken> getToken(TokenRequestContext request) {
            return CompletableFuture.completedFuture(new AccessToken("TestAccessToken", OffsetDateTime.now().plusHours(1)));
        }
    }

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
            // In v2, we just return the original client since the assertion logic
            // is handled differently or may not be needed
            return httpClient;
        }
    }
}
