// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.credential;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link SimpleTokenCache}.
 */
public class SimpleTokenCacheTests {

    public static void main(String[] args) {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            return Mono.just(new TokenCacheTests.Token("testToken", 30000, 1000));
        };

        SimpleTokenCache cache = new SimpleTokenCache(() -> dummyCred.getToken(new TokenRequestContext()));

        cache.setRefreshDelay(Duration.ofSeconds(0));

        StepVerifier.create(cache.getToken().delayElement(Duration.ofMillis(2000)).flatMap(ignored -> cache.getToken()))
            .assertNext(token -> {
                assertEquals("testToken", token.getToken());
                assertEquals(2, refreshes.get());
            })
            .verifyComplete();
    }

    @Test
    public void wipResetsOnCancel() {
        SimpleTokenCache simpleTokenCache
            = new SimpleTokenCache(() -> Mono.just(new AccessToken("test", OffsetDateTime.now().plusMinutes(5)))
                .delayElement(Duration.ofMinutes(1)));

        StepVerifier
            .create(simpleTokenCache.getToken().doOnRequest(ignored -> assertNotNull(simpleTokenCache.getWipValue())))
            .expectSubscription()
            .expectNoEvent(Duration.ofSeconds(2))
            .thenCancel()
            .verify();

        assertNull(simpleTokenCache.getWipValue());
    }

    @Test
    public void testRefreshOnFlow() throws InterruptedException {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            return Mono.just(new TokenCacheTests.Token("testToken", 30000, 1000));
        };

        SimpleTokenCache cache = new SimpleTokenCache(() -> dummyCred.getToken(new TokenRequestContext()));

        cache.setRefreshDelay(Duration.ofSeconds(0));

        StepVerifier.create(cache.getToken().delayElement(Duration.ofMillis(2000)).flatMap(ignored -> cache.getToken()))
            .assertNext(token -> {
                assertEquals("testToken", token.getToken());
                assertEquals(2, refreshes.get());
            })
            .verifyComplete();
    }

    @Test
    public void testDoNotRefreshOnFlow() throws InterruptedException {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            return Mono.just(new TokenCacheTests.Token("testToken", 30000, 4000));
        };

        SimpleTokenCache cache = new SimpleTokenCache(() -> dummyCred.getToken(new TokenRequestContext()));

        cache.setRefreshDelay(Duration.ofSeconds(0));

        StepVerifier.create(cache.getToken().delayElement(Duration.ofMillis(2000)).flatMap(ignored -> cache.getToken()))
            .assertNext(token -> {
                assertEquals("testToken", token.getToken());
                assertEquals(1, refreshes.get());
            })
            .verifyComplete();
    }
}
