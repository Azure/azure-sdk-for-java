// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link AccessTokenCache} specifically focused on tenant ID matching logic
 * and force refresh scenarios.
 */
public class AccessTokenCacheTests {

    /**
     * Creates a TokenCredential that tracks refresh count and returns tokens with incrementing suffixes.
     */
    private TokenCredential createMockCredential(AtomicLong refreshCount) {
        return request -> {
            refreshCount.incrementAndGet();
            return Mono.just(new AccessToken("token-" + refreshCount.get(), OffsetDateTime.now().plusHours(1)));
        };
    }

    /**
     * Creates a synchronous TokenCredential that tracks refresh count.
     */
    private TokenCredential createMockSyncCredential(AtomicLong refreshCount) {
        return new TokenCredential() {
            @Override
            public Mono<AccessToken> getToken(TokenRequestContext request) {
                return Mono.just(getTokenSync(request));
            }

            @Override
            public AccessToken getTokenSync(TokenRequestContext request) {
                refreshCount.incrementAndGet();
                return new AccessToken("sync-token-" + refreshCount.get(), OffsetDateTime.now().plusHours(1));
            }
        };
    }

    /**
     * Creates a TokenCredential with a delay that tracks refresh count and returns tokens with incrementing suffixes.
     */
    private TokenCredential createMockCredentialWithDelay(AtomicInteger refreshCount, Duration delay) {
        return request -> {
            int count = refreshCount.incrementAndGet();
            return Mono.just(new AccessToken("token-" + count, OffsetDateTime.now().plusHours(1))).delayElement(delay);
        };
    }

    private static final String SCOPE = "https://management.azure.com/.default";
    private static final String TENANT_ID_1 = "tenant-1";
    private static final String TENANT_ID_2 = "tenant-2";
    private static final String CLAIMS = "{\"access_token\":{\"nbf\":{\"essential\":true,\"value\":\"1658241070\"}}}";

    @Test
    public void testTenantIdChangeTriggersForceRefresh() {
        AtomicLong refreshCount = new AtomicLong(0);
        TokenCredential credential = createMockCredential(refreshCount);
        AccessTokenCache cache = new AccessTokenCache(credential);

        // First request with tenant1
        TokenRequestContext context1 = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_1);

        StepVerifier.create(cache.getToken(context1, true)).assertNext(token -> {
            assertNotNull(token);
            assertEquals("token-1", token.getToken());
        }).verifyComplete();

        assertEquals(1, refreshCount.get());

        // Second request with same tenant should not trigger refresh (checkToForceFetchToken=false)
        StepVerifier.create(cache.getToken(context1, false)).assertNext(token -> {
            assertNotNull(token);
            assertEquals("token-1", token.getToken()); // Same token
        }).verifyComplete();

        assertEquals(1, refreshCount.get()); // No additional refresh

        // Third request with different tenant should trigger force refresh with checkToForceFetchToken=true
        TokenRequestContext context2 = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_2);

        StepVerifier.create(cache.getToken(context2, true)).assertNext(token -> {
            assertNotNull(token);
            assertEquals("token-2", token.getToken()); // New token
        }).verifyComplete();

        assertEquals(2, refreshCount.get()); // Additional refresh due to tenant change
    }

    @Test
    public void testTenantIdChangeTriggersForceRefreshSync() {
        AtomicLong refreshCount = new AtomicLong(0);
        TokenCredential credential = createMockSyncCredential(refreshCount);
        AccessTokenCache cache = new AccessTokenCache(credential);

        // First request with tenant1
        TokenRequestContext context1 = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_1);

        AccessToken token1 = cache.getTokenSync(context1, true);
        assertEquals("sync-token-1", token1.getToken());
        assertEquals(1, refreshCount.get());

        // Second request with same tenant should not trigger refresh
        AccessToken token2 = cache.getTokenSync(context1, false);
        assertEquals("sync-token-1", token2.getToken()); // Same token
        assertEquals(1, refreshCount.get()); // No additional refresh

        // Third request with different tenant should trigger force refresh
        TokenRequestContext context2 = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_2);

        AccessToken token3 = cache.getTokenSync(context2, true);
        assertEquals("sync-token-2", token3.getToken()); // New token
        assertEquals(2, refreshCount.get()); // Additional refresh due to tenant change
    }

    @Test
    public void testNullTenantIdHandling() {
        AtomicLong refreshCount = new AtomicLong(0);
        TokenCredential credential = createMockCredential(refreshCount);
        AccessTokenCache cache = new AccessTokenCache(credential);

        // First request with null tenant
        TokenRequestContext context1 = new TokenRequestContext().addScopes(SCOPE);
        // tenantId is null by default

        StepVerifier.create(cache.getToken(context1, true))
            .assertNext(token -> assertEquals("token-1", token.getToken()))
            .verifyComplete();

        assertEquals(1, refreshCount.get());

        // Second request with null tenant should not trigger refresh
        StepVerifier.create(cache.getToken(context1, false))
            .assertNext(token -> assertEquals("token-1", token.getToken()))
            .verifyComplete();

        assertEquals(1, refreshCount.get());

        // Third request with actual tenant should trigger refresh
        TokenRequestContext context2 = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_1);

        StepVerifier.create(cache.getToken(context2, true))
            .assertNext(token -> assertEquals("token-2", token.getToken()))
            .verifyComplete();

        assertEquals(2, refreshCount.get());

        // Fourth request back to null tenant should trigger refresh
        TokenRequestContext context3 = new TokenRequestContext().addScopes(SCOPE);

        StepVerifier.create(cache.getToken(context3, true))
            .assertNext(token -> assertEquals("token-3", token.getToken()))
            .verifyComplete();

        assertEquals(3, refreshCount.get());
    }

    @Test
    public void testTenantIdWithClaimsAndScopesCombined() {
        AtomicLong refreshCount = new AtomicLong(0);
        TokenCredential credential = createMockCredential(refreshCount);
        AccessTokenCache cache = new AccessTokenCache(credential);

        // Initial request
        TokenRequestContext context1
            = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_1).setClaims(CLAIMS);

        StepVerifier.create(cache.getToken(context1, true))
            .assertNext(token -> assertEquals("token-1", token.getToken()))
            .verifyComplete();

        assertEquals(1, refreshCount.get());

        // Same tenant, same claims, same scopes - should not refresh
        TokenRequestContext context2
            = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_1).setClaims(CLAIMS);

        StepVerifier.create(cache.getToken(context2, false))
            .assertNext(token -> assertEquals("token-1", token.getToken()))
            .verifyComplete();

        assertEquals(1, refreshCount.get());

        // Different tenant, same claims, same scopes - should refresh
        TokenRequestContext context3
            = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_2).setClaims(CLAIMS);

        StepVerifier.create(cache.getToken(context3, true))
            .assertNext(token -> assertEquals("token-2", token.getToken()))
            .verifyComplete();

        assertEquals(2, refreshCount.get());

        // Same tenant as before, different claims - should refresh
        TokenRequestContext context4 = new TokenRequestContext().addScopes(SCOPE)
            .setTenantId(TENANT_ID_2)
            .setClaims("{\"different\":\"claims\"}");

        StepVerifier.create(cache.getToken(context4, true))
            .assertNext(token -> assertEquals("token-3", token.getToken()))
            .verifyComplete();

        assertEquals(3, refreshCount.get());

        // Same tenant, same claims as context4, different scopes - should refresh
        TokenRequestContext context5 = new TokenRequestContext().addScopes("https://graph.microsoft.com/.default")
            .setTenantId(TENANT_ID_2)
            .setClaims("{\"different\":\"claims\"}");

        StepVerifier.create(cache.getToken(context5, true))
            .assertNext(token -> assertEquals("token-4", token.getToken()))
            .verifyComplete();

        assertEquals(4, refreshCount.get());
    }

    @Test
    public void testConcurrentRequestsWithDifferentTenants() {
        AtomicInteger refreshCount = new AtomicInteger(0);

        TokenCredential credential = createMockCredentialWithDelay(refreshCount, Duration.ofMillis(100));

        AccessTokenCache cache = new AccessTokenCache(credential);

        TokenRequestContext contextTenant1 = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_1);

        TokenRequestContext contextTenant2 = new TokenRequestContext().addScopes(SCOPE).setTenantId(TENANT_ID_2);

        // Initial request to establish cache
        StepVerifier.create(cache.getToken(contextTenant1, true))
            .assertNext(token -> assertEquals("token-1", token.getToken()))
            .verifyComplete();

        // Concurrent requests: one for same tenant, one for different tenant
        StepVerifier.create(Mono.zip(cache.getToken(contextTenant1, false), // Should use cache
            cache.getToken(contextTenant2, true)  // Should force refresh due to different tenant
        )).assertNext(tuple -> {
            AccessToken token1 = tuple.getT1();
            AccessToken token2 = tuple.getT2();

            // One should be cached, one should be new
            assertEquals("token-1", token1.getToken());
            assertEquals("token-2", token2.getToken());
        }).verifyComplete();

        assertEquals(2, refreshCount.get());
    }

    @Test
    public void testEmptyStringTenantIdTreatedAsDifferentFromNull() {
        AtomicLong refreshCount = new AtomicLong(0);
        TokenCredential credential = createMockCredential(refreshCount);
        AccessTokenCache cache = new AccessTokenCache(credential);

        // First request with null tenant
        TokenRequestContext contextNull = new TokenRequestContext().addScopes(SCOPE);

        StepVerifier.create(cache.getToken(contextNull, true))
            .assertNext(token -> assertEquals("token-1", token.getToken()))
            .verifyComplete();

        assertEquals(1, refreshCount.get());

        // Second request with empty string tenant - should trigger refresh
        TokenRequestContext contextEmpty = new TokenRequestContext().addScopes(SCOPE).setTenantId("");

        StepVerifier.create(cache.getToken(contextEmpty, true))
            .assertNext(token -> assertEquals("token-2", token.getToken()))
            .verifyComplete();

        assertEquals(2, refreshCount.get());

        // Third request with empty string again - should not refresh
        StepVerifier.create(cache.getToken(contextEmpty, false))
            .assertNext(token -> assertEquals("token-2", token.getToken()))
            .verifyComplete();

        assertEquals(2, refreshCount.get());
    }
}
