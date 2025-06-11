// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.certificates;

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
import io.clientcore.core.http.models.Response;
import io.clientcore.core.util.BinaryData;
import io.clientcore.core.util.Context;
import com.azure.v2.security.keyvault.certificates.implementation.KeyVaultCredentialPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.http.models.HttpHeaderName.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultCredentialPolicyTest {
    private static final String VAULT_URL = "https://test.vault.azure.net";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final HttpHeaderName AUTHENTICATE_HEADER = HttpHeaderName.fromString("WWW-Authenticate");
    private static final String AUTHENTICATE_HEADER_VALUE = "Bearer authorization_uri=\"https://login.windows.net/0287f963-2926-4d11-8245-7c654d965a72\", resource=\"https://vault.azure.net\"";

    private KeyVaultCredentialPolicy policy;
    private MockTokenCredential credential;
    private MockHttpClient mockHttpClient;

    @BeforeEach
    public void setUp() {
        credential = new MockTokenCredential();
        policy = new KeyVaultCredentialPolicy(credential);
        mockHttpClient = new MockHttpClient();
    }

    @AfterEach
    public void tearDown() {
        if (mockHttpClient != null) {
            mockHttpClient.reset();
        }
    }

    @Test
    public void onChallengeCredentialPolicy() {
        // Create a request
        HttpRequest request = new HttpRequest(HttpMethod.GET, VAULT_URL + "/secrets/secret1");

        // Create an unauthorized response with challenge
        Response<?> challengeResponse = createChallengeResponse();

        // Create context and pipeline call context
        Context context = new Context("key", "value");
        HttpPipelineCallContext callContext = new HttpPipelineCallContext(request, context);

        // Process the challenge
        boolean result = policy.onChallenge(callContext, challengeResponse);

        assertTrue(result);

        // Verify the authorization header was added
        String authHeader = request.getHeaders().getValue(AUTHORIZATION);
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith(BEARER_TOKEN_PREFIX));
    }

    @Test
    public void onChallengeCredentialPolicyEmptyHeader() {
        // Create a request
        HttpRequest request = new HttpRequest(HttpMethod.GET, VAULT_URL + "/secrets/secret1");

        // Create an unauthorized response without challenge header
        Response<?> challengeResponse = createEmptyChallengeResponse();

        // Create context and pipeline call context
        Context context = new Context("key", "value");
        HttpPipelineCallContext callContext = new HttpPipelineCallContext(request, context);

        // Process the challenge
        boolean result = policy.onChallenge(callContext, challengeResponse);

        assertFalse(result);
    }

    @Test
    public void testCredentialPolicyTokenRefresh() {
        // Create a request
        HttpRequest request = new HttpRequest(HttpMethod.GET, VAULT_URL + "/secrets/secret1");

        // Create an unauthorized response with challenge
        Response<?> challengeResponse = createChallengeResponse();

        // Create context and pipeline call context
        Context context = new Context("key", "value");
        HttpPipelineCallContext callContext = new HttpPipelineCallContext(request, context);

        // Set an expired token
        credential.setToken(new AccessToken("expired-token", OffsetDateTime.now().minusHours(1)));

        // Process the challenge
        boolean result = policy.onChallenge(callContext, challengeResponse);

        assertTrue(result);

        // Verify a new token was requested
        assertTrue(credential.getTokenCallCount() > 0);

        // Verify the authorization header was updated
        String authHeader = request.getHeaders().getValue(AUTHORIZATION);
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith(BEARER_TOKEN_PREFIX));
        assertNotEquals(BEARER_TOKEN_PREFIX + "expired-token", authHeader);
    }

    @Test
    public void testCredentialPolicyMultipleChallenges() {
        AtomicInteger callCount = new AtomicInteger(0);

        // Create requests for multiple vaults
        HttpRequest request1 = new HttpRequest(HttpMethod.GET, "https://vault1.vault.azure.net/secrets/secret1");
        HttpRequest request2 = new HttpRequest(HttpMethod.GET, "https://vault2.vault.azure.net/secrets/secret2");

        // Create challenge responses
        Response<?> challengeResponse1 = createChallengeResponse();
        Response<?> challengeResponse2 = createChallengeResponse();

        // Create contexts
        Context context = new Context("key", "value");
        HttpPipelineCallContext callContext1 = new HttpPipelineCallContext(request1, context);
        HttpPipelineCallContext callContext2 = new HttpPipelineCallContext(request2, context);

        // Process challenges
        boolean result1 = policy.onChallenge(callContext1, challengeResponse1);
        boolean result2 = policy.onChallenge(callContext2, challengeResponse2);

        assertTrue(result1);
        assertTrue(result2);

        // Verify both requests have authorization headers
        assertNotNull(request1.getHeaders().getValue(AUTHORIZATION));
        assertNotNull(request2.getHeaders().getValue(AUTHORIZATION));
    }

    private Response<?> createChallengeResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHENTICATE_HEADER, AUTHENTICATE_HEADER_VALUE);

        return new MockResponse(401, headers);
    }

    private Response<?> createEmptyChallengeResponse() {
        return new MockResponse(401, new HttpHeaders());
    }

    private static class MockTokenCredential implements TokenCredential {
        private AccessToken token;
        private AtomicInteger tokenCallCount = new AtomicInteger(0);

        public void setToken(AccessToken token) {
            this.token = token;
        }

        public int getTokenCallCount() {
            return tokenCallCount.get();
        }

        @Override
        public CompletableFuture<AccessToken> getToken(TokenRequestContext request) {
            tokenCallCount.incrementAndGet();
            if (token == null) {
                token = new AccessToken("mock-token", OffsetDateTime.now().plusHours(1));
            }
            return CompletableFuture.completedFuture(token);
        }
    }

    private static class MockResponse implements Response<Void> {
        private final int statusCode;
        private final HttpHeaders headers;

        public MockResponse(int statusCode, HttpHeaders headers) {
            this.statusCode = statusCode;
            this.headers = headers;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getHeaderValue(String name) {
            return headers.getValue(name);
        }

        @Override
        public String getHeaderValue(HttpHeaderName headerName) {
            return headers.getValue(headerName);
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public Void getValue() {
            return null;
        }

        @Override
        public BinaryData getBody() {
            return null;
        }

        @Override
        public HttpRequest getRequest() {
            return null;
        }

        @Override
        public void close() {
            // No-op
        }
    }

    private static class MockHttpClient {
        private List<Response<?>> responses = new ArrayList<>();
        private AtomicInteger callCount = new AtomicInteger(0);

        public void addResponse(Response<?> response) {
            responses.add(response);
        }

        public int getCallCount() {
            return callCount.get();
        }

        public void reset() {
            responses.clear();
            callCount.set(0);
        }
    }
}
