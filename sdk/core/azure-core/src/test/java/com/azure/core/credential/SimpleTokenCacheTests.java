// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.credential;

import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SimpleTokenCache}.
 */
public class SimpleTokenCacheTests {

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
    public void waitingCallerGetsTokenWhenRefreshingCallerIsCancelled() throws InterruptedException {
        // The first token request never completes, any later one succeeds.
        AtomicInteger refreshCount = new AtomicInteger(0);
        CountDownLatch firstRequestStarted = new CountDownLatch(1);
        SimpleTokenCache simpleTokenCache = new SimpleTokenCache(() -> {
            if (refreshCount.incrementAndGet() == 1) {
                firstRequestStarted.countDown();
                return Mono.never();
            }
            return Mono.just(new AccessToken("token-" + refreshCount.get(), OffsetDateTime.now().plusHours(1)));
        }, Duration.ZERO);

        // The first caller starts the token refresh, the second caller waits on that refresh.
        Disposable refreshingCaller = simpleTokenCache.getToken().subscribe();
        // A new cache starts its first token request asynchronously.
        assertTrue(firstRequestStarted.await(10, TimeUnit.SECONDS));
        Mono<AccessToken> waitingCaller = simpleTokenCache.getToken();

        StepVerifier.create(waitingCaller)
            .then(refreshingCaller::dispose)
            .assertNext(token -> assertEquals("token-2", token.getToken()))
            .expectComplete()
            .verify(Duration.ofSeconds(10));

        assertEquals(2, refreshCount.get());
    }

    @Test
    public void waitingCallerGetsTokenWhenRefreshCompletesEmpty() throws InterruptedException {
        // The first token request completes empty when the test says so, any later one succeeds.
        Sinks.One<AccessToken> firstRequest = Sinks.one();
        AtomicInteger refreshCount = new AtomicInteger(0);
        CountDownLatch firstRequestStarted = new CountDownLatch(1);
        SimpleTokenCache simpleTokenCache = new SimpleTokenCache(() -> {
            if (refreshCount.incrementAndGet() == 1) {
                firstRequestStarted.countDown();
                return firstRequest.asMono();
            }
            return Mono.just(new AccessToken("token-" + refreshCount.get(), OffsetDateTime.now().plusHours(1)));
        }, Duration.ZERO);

        // The first caller starts the token refresh, the second caller waits on that refresh.
        Disposable refreshingCaller = simpleTokenCache.getToken().subscribe();
        // A new cache starts its first token request asynchronously.
        assertTrue(firstRequestStarted.await(10, TimeUnit.SECONDS));
        Mono<AccessToken> waitingCaller = simpleTokenCache.getToken();

        StepVerifier.create(waitingCaller)
            .then(firstRequest::tryEmitEmpty)
            .assertNext(token -> assertEquals("token-2", token.getToken()))
            .expectComplete()
            .verify(Duration.ofSeconds(10));

        assertEquals(2, refreshCount.get());
        refreshingCaller.dispose();
    }

    @Test
    public void testRefreshOnFlow() throws InterruptedException {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            return Mono.just(new TokenCacheTests.Token("testToken", 30000, 1000));
        };

        SimpleTokenCache cache
            = new SimpleTokenCache(() -> dummyCred.getToken(new TokenRequestContext()), Duration.ofSeconds(0));

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
            return Mono.just(new TokenCacheTests.Token("testToken", 30000, 12000));
        };

        SimpleTokenCache cache
            = new SimpleTokenCache(() -> dummyCred.getToken(new TokenRequestContext()), Duration.ofSeconds(1));

        StepVerifier.create(cache.getToken().delayElement(Duration.ofMillis(2000)).flatMap(ignored -> cache.getToken()))
            .assertNext(token -> {
                assertEquals("testToken", token.getToken());
                assertEquals(1, refreshes.get());
            })
            .verifyComplete();
    }
}
