// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.clientcore.core.http.models.HttpHeaderName.AUTHORIZATION;
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
        policy = new KeyVaultCredentialPolicy(credential, false);
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
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(VAULT_URL + "/roles");

        // Create an unauthorized response with challenge
        Response<?> challengeResponse = createChallengeResponse();

        // Process the challenge
        boolean result = policy.authorizeRequestOnChallenge(request, challengeResponse);

        assertTrue(result);

        // Verify the authorization header was added
        String authHeader = request.getHeaders().getValue(AUTHORIZATION);

        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith(BEARER_TOKEN_PREFIX));
    }

    @Test
    public void onChallengeCredentialPolicyEmptyHeader() {
        // Create a request
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(VAULT_URL + "/roles");

        // Create an unauthorized response without challenge header
        Response<?> challengeResponse = createEmptyChallengeResponse();

        // Process the challenge
        boolean result = policy.authorizeRequestOnChallenge(request, challengeResponse);

        assertFalse(result);
    }

    @Test
    public void testCredentialPolicyTokenRefresh() {
        // Create a request
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri(VAULT_URL + "/backup");

        // Create an unauthorized response with challenge
        Response<?> challengeResponse = createChallengeResponse();

        // Set an expired token
        credential.setToken(new AccessToken("expired-token", OffsetDateTime.now().minusHours(1)));

        // Process the challenge
        boolean result = policy.authorizeRequestOnChallenge(request, challengeResponse);

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

        // Create requests for multiple HSM operations
        HttpRequest request1 = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://hsm1.vault.azure.net/backup");
        HttpRequest request2 = new HttpRequest()
            .setMethod(HttpMethod.GET)
            .setUri("https://hsm2.vault.azure.net/roles");

        // Create challenge responses
        Response<?> challengeResponse1 = createChallengeResponse();
        Response<?> challengeResponse2 = createChallengeResponse();

        // Process challenges
        boolean result1 = policy.authorizeRequestOnChallenge(request1, challengeResponse1);
        boolean result2 = policy.authorizeRequestOnChallenge(request2, challengeResponse2);

        assertTrue(result1);
        assertTrue(result2);

        // Verify both requests have authorization headers
        assertNotNull(request1.getHeaders().getValue(AUTHORIZATION));
        assertNotNull(request2.getHeaders().getValue(AUTHORIZATION));
    }

    @Test
    public void testCredentialPolicyWithRoleAssignment() {
        // Create a request for role assignment
        HttpRequest request = new HttpRequest()
            .setMethod(HttpMethod.PUT)
            .setUri(VAULT_URL + "/providers/Microsoft.Authorization/roleAssignments/test-assignment");

        // Create an unauthorized response with challenge
        Response<?> challengeResponse = createChallengeResponse();

        // Process the challenge
        boolean result = policy.authorizeRequestOnChallenge(request, challengeResponse);

        assertTrue(result);

        // Verify the authorization header was added
        String authHeader = request.getHeaders().getValue(AUTHORIZATION);

        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith(BEARER_TOKEN_PREFIX));
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
        private final AtomicInteger tokenCallCount = new AtomicInteger(0);
        private AccessToken token;

        public void setToken(AccessToken token) {
            this.token = token;
        }

        public int getTokenCallCount() {
            return tokenCallCount.get();
        }

        @Override
        public AccessToken getToken(TokenRequestContext request) {
            tokenCallCount.incrementAndGet();

            if (token == null) {
                token = new AccessToken("mock-token", OffsetDateTime.now().plusHours(1));
            }

            return token;
        }
    }

    private static class MockResponse extends Response<Void> {
        public MockResponse(int statusCode, HttpHeaders headers) {
            super(null, statusCode, headers, null);
        }

        @Override
        public void close() {
            // No-op
        }
    }

    private static class MockHttpClient {
        private final List<Response<?>> responses = new ArrayList<>();
        private final AtomicInteger callCount = new AtomicInteger(0);

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
