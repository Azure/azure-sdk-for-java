// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for ChainedTokenCredential.
 */
public class ChainedTokenCredentialTest {

    private static final TokenRequestContext REQUEST
        = new TokenRequestContext().addScopes("https://management.azure.com/.default");

    // Simple TokenCredential implementation that returns a predefined token
    private static class TestTokenCredential implements TokenCredential {
        private final String tokenValue;
        private final boolean shouldFail;
        private final boolean unavailable;
        private final AtomicInteger callCount = new AtomicInteger(0);

        TestTokenCredential(String tokenValue, boolean shouldFail) {
            this(tokenValue, shouldFail, false);
        }

        TestTokenCredential(String tokenValue, boolean shouldFail, boolean unavailable) {
            this.tokenValue = tokenValue;
            this.shouldFail = shouldFail;
            this.unavailable = unavailable;
        }

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext request) {
            callCount.incrementAndGet();
            if (shouldFail) {
                if (unavailable) {
                    return Mono.error(new CredentialUnavailableException("Credential unavailable"));
                } else {
                    return Mono.error(new ClientAuthenticationException("Authentication failed", null));
                }
            } else {
                return Mono.just(new AccessToken(tokenValue, OffsetDateTime.now().plusHours(1)));
            }
        }

        public int getCallCount() {
            return callCount.get();
        }
    }

    @Test
    public void testConstructorRequiresCredentials() {
        // Test that attempting to build a ChainedTokenCredential without adding any credentials
        // throws an IllegalStateException
        IllegalStateException exception
            = assertThrows(IllegalStateException.class, () -> new ChainedTokenCredentialBuilder().build());

        // Verify the exception message
        assertTrue(exception.getMessage().contains("At least one credential must be added to the chain."));
    }

    @Test
    public void testFirstCredentialWorks() {
        // Setup
        TestTokenCredential firstCredential = new TestTokenCredential("token1", false);
        TestTokenCredential secondCredential = new TestTokenCredential("token2", false);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(firstCredential).addLast(secondCredential).build();

        // Test
        StepVerifier.create(chain.getToken(REQUEST)).assertNext(token -> {
            assertNotNull(token);
            assertEquals("token1", token.getToken());
        }).verifyComplete();

        // Second credential should not be called
        assertEquals(1, firstCredential.getCallCount());
        assertEquals(0, secondCredential.getCallCount());
    }

    @Test
    public void testFallbackToSecondCredential() {
        // Setup
        TestTokenCredential firstCredential = new TestTokenCredential("token1", true, true);
        TestTokenCredential secondCredential = new TestTokenCredential("token2", false);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(firstCredential).addLast(secondCredential).build();

        // Test
        StepVerifier.create(chain.getToken(REQUEST)).assertNext(token -> {
            assertNotNull(token);
            assertEquals("token2", token.getToken());
        }).verifyComplete();

        // Both credentials should be called
        assertEquals(1, firstCredential.getCallCount());
        assertEquals(1, secondCredential.getCallCount());
    }

    @Test
    public void testFallbackAfterCredentialUnavailable() {
        // Setup
        TestTokenCredential firstCredential = new TestTokenCredential("token1", true, true);
        TestTokenCredential secondCredential = new TestTokenCredential("token2", false);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(firstCredential).addLast(secondCredential).build();

        // Test
        StepVerifier.create(chain.getToken(REQUEST)).assertNext(token -> {
            assertNotNull(token);
            assertEquals("token2", token.getToken());
        }).verifyComplete();

        // Both credentials should be called
        assertEquals(1, firstCredential.getCallCount());
        assertEquals(1, secondCredential.getCallCount());
    }

    @Test
    public void testAllCredentialsFail() {
        // Setup - authentication failures
        TestTokenCredential firstCredential = new TestTokenCredential("token1", true);
        TestTokenCredential secondCredential = new TestTokenCredential("token2", true);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(firstCredential).addLast(secondCredential).build();

        // Test
        StepVerifier.create(chain.getToken(REQUEST))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException
                && e.getMessage().contains("ChainedTokenCredential authentication failed"))
            .verify();
    }

    @Test
    public void testAllCredentialsUnavailable() {
        // Setup
        TestTokenCredential firstCredential = new TestTokenCredential("token1", true, true);
        TestTokenCredential secondCredential = new TestTokenCredential("token2", true, true);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(firstCredential).addLast(secondCredential).build();

        // Test
        StepVerifier.create(chain.getToken(REQUEST))
            .expectErrorMatches(
                e -> e instanceof CredentialUnavailableException && e.getMessage().contains("Credential unavailable"))
            .verify();
    }

    @Test
    public void testMixedExceptionsEndWithAuthentication() {
        // Setup - first credential unavailable, second fails authentication
        TestTokenCredential firstCredential = new TestTokenCredential("token1", true, true);
        TestTokenCredential secondCredential = new TestTokenCredential("token2", true, false);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(firstCredential).addLast(secondCredential).build();

        // Test
        StepVerifier.create(chain.getToken(REQUEST))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException
                && e.getMessage().contains("ChainedTokenCredential authentication failed"))
            .verify();
    }

    @Test
    public void testMixedExceptionsEndWithUnavailable() {
        // Setup - first fails authentication, second credential unavailable
        TestTokenCredential firstCredential = new TestTokenCredential("token1", true, false);
        TestTokenCredential secondCredential = new TestTokenCredential("token2", true, true);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(firstCredential).addLast(secondCredential).build();

        // Test
        StepVerifier.create(chain.getToken(REQUEST))
            .expectErrorMatches(e -> e instanceof ClientAuthenticationException
                && e.getMessage().contains("ChainedTokenCredential authentication failed"))
            .verify();
    }

    @Test
    public void testMultipleCredentials() {
        // Setup - first two unavailable, third works
        TestTokenCredential first = new TestTokenCredential("token1", true, true);
        TestTokenCredential second = new TestTokenCredential("token2", true, true);
        TestTokenCredential third = new TestTokenCredential("token3", false);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(first).addLast(second).addLast(third).build();

        // Test
        StepVerifier.create(chain.getToken(REQUEST)).assertNext(token -> {
            assertNotNull(token);
            assertEquals("token3", token.getToken());
        }).verifyComplete();

        // All three credentials should be called in order
        assertEquals(1, first.getCallCount());
        assertEquals(1, second.getCallCount());
        assertEquals(1, third.getCallCount());
    }

    @Test
    public void testErrorMessageChaining() {
        // Setup - both credentials unavailable
        TestTokenCredential first = new TestTokenCredential("token1", true, true);
        TestTokenCredential second = new TestTokenCredential("token2", true, true);

        ChainedTokenCredential chain = new ChainedTokenCredentialBuilder().addFirst(first).addLast(second).build();

        // Test
        try {
            chain.getToken(REQUEST).block();
        } catch (CredentialUnavailableException e) {
            // Verify error message contains both credential errors
            assertTrue(e.getMessage().contains("Credential unavailable"));
            // Should contain the troubleshooting URL
            assertTrue(
                e.getMessage().contains("https://aka.ms/azure-identity-java-default-azure-credential-troubleshoot"));
        }
    }

    @Test
    public void testSyncTokenRetrieval() {
        // Setup
        TestTokenCredential firstCredential = new TestTokenCredential("token1", true, true);
        TestTokenCredential secondCredential = new TestTokenCredential("token2", false);

        ChainedTokenCredential chain
            = new ChainedTokenCredentialBuilder().addFirst(firstCredential).addLast(secondCredential).build();

        // Test
        AccessToken token = chain.getTokenSync(REQUEST);

        assertNotNull(token);
        assertEquals("token2", token.getToken());

        // Both credentials should be called
        assertEquals(1, firstCredential.getCallCount());
        assertEquals(1, secondCredential.getCallCount());
    }

    @Test
    public void testDifferentScopesUsesDifferentTokens() {
        // Setup
        AtomicInteger callCount = new AtomicInteger(0);

        TokenCredential credential = new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext request) {
                callCount.incrementAndGet();
                String token = "token-for-" + String.join(",", request.getScopes());
                return Mono.just(new AccessToken(token, OffsetDateTime.now().plusHours(1)));
            }
        };

        ChainedTokenCredential chain = new ChainedTokenCredentialBuilder().addLast(credential).build();

        // Create different requests with different scopes
        TokenRequestContext request1 = new TokenRequestContext().addScopes("scope1");
        TokenRequestContext request2 = new TokenRequestContext().addScopes("scope2");

        // Test
        AccessToken token1 = chain.getToken(request1).block();
        AccessToken token2 = chain.getToken(request2).block();

        assertNotNull(token1);
        assertNotNull(token2);
        assertEquals("token-for-scope1", token1.getToken());
        assertEquals("token-for-scope2", token2.getToken());

        // Both requests should cause credential to be called as they have different scopes
        assertEquals(2, callCount.get());
    }
}
