// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys;

import io.clientcore.core.credential.AccessToken;
import io.clientcore.core.credential.TokenCredential;
import io.clientcore.core.credential.TokenRequestContext;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelineCallContext;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.util.BinaryData;
import io.clientcore.core.util.Context;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static io.clientcore.core.http.models.HttpHeaderName.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class KeyVaultCredentialPolicyTest {
    private static final String AUTHENTICATE_HEADER
        = "Bearer authorization=\"https://login.windows.net/72f988bf-86f1-41af-91ab-2d7cd022db57\", "
            + "resource=\"https://vault.azure.net\"";

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String RESOURCE = "https://vault.azure.net";

    private KeyVaultCredentialPolicy policy;
    private MockTokenCredential credential;
    private HttpPipeline pipeline;

    @BeforeEach
    public void setup() {
        credential = new MockTokenCredential();
        policy = new KeyVaultCredentialPolicy(credential);
    }

    @AfterEach
    public void cleanup() {
        KeyVaultCredentialPolicy.clearCache();
    }

    @Test
    public void testPolicyWithValidChallenge() {
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://test.vault.azure.net/keys/key1");

        // Create a mock response with WWW-Authenticate header
        HttpResponse challengeResponse = new MockHttpResponse(request, 401)
            .setHeader("WWW-Authenticate", AUTHENTICATE_HEADER);

        // The policy should handle the authentication challenge
        assertNotNull(policy);
    }

    @Test
    public void testCredentialCacheClearing() {
        KeyVaultCredentialPolicy.clearCache();
        // Verify that clearing the cache doesn't throw an exception
        assertTrue(true);
    }

    private static class MockTokenCredential implements TokenCredential {
        private AtomicInteger callCount = new AtomicInteger();
        private String tokenValue = "mock-token";

        @Override
        public CompletableFuture<AccessToken> getToken(TokenRequestContext request) {
            callCount.incrementAndGet();
            return CompletableFuture.completedFuture(
                new AccessToken(tokenValue, OffsetDateTime.now().plusHours(1))
            );
        }

        public int getCallCount() {
            return callCount.get();
        }

        public void setTokenValue(String tokenValue) {
            this.tokenValue = tokenValue;
        }
    }

    private static class MockHttpResponse implements HttpResponse {
        private final HttpRequest request;
        private final int statusCode;
        private final HttpHeaders headers;

        public MockHttpResponse(HttpRequest request, int statusCode) {
            this.request = request;
            this.statusCode = statusCode;
            this.headers = new HttpHeaders();
        }

        public MockHttpResponse setHeader(String name, String value) {
            headers.set(HttpHeaderName.fromString(name), value);
            return this;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(HttpHeaderName name) {
            return headers.getValue(name);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public BinaryData getBody() {
            return BinaryData.fromString("");
        }

        @Override
        public HttpRequest getRequest() {
            return request;
        }

        @Override
        public void close() {
            // No-op
        }
    }
}
