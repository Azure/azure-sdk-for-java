// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.AccessToken;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.http.models.HttpPipelineCallContext;
import io.clientcore.core.http.models.HttpPipelineNextPolicy;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePosition;

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
        public CompletableFuture<Response<Void>> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
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
        public CompletableFuture<Response<Void>> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
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
}
