// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenCacheTests {

    @BeforeEach
    void beforeEach() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }
    @Test
    public void testOnlyOneThreadRefreshesToken() {
        AtomicLong refreshes = new AtomicLong(0);

        // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
        SimpleTokenCache cache = new SimpleTokenCache(() -> {
            refreshes.incrementAndGet();
            return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
        });

        StepVerifier.create(Flux.range(1, 10).flatMap(ignored -> Mono.just(OffsetDateTime.now()))
            .parallel(10)
                // Runs cache.getToken() on 10 different threads
                .runOn(Schedulers.boundedElastic())
                .flatMap(start -> cache.getToken())
                .then())
            .verifyComplete();

        // Ensure that only one refresh attempt is made.
        assertEquals(1, refreshes.get());
    }

    @Test
    public void testLongRunningWontOverflow() throws Exception {
        AtomicLong refreshes = new AtomicLong(0);

        // token expires on creation. Run this 100 times to simulate running the application a long time
        SimpleTokenCache cache = new SimpleTokenCache(() -> {
            refreshes.incrementAndGet();
            return remoteGetTokenThatExpiresSoonAsync();
        });

        VirtualTimeScheduler virtualTimeScheduler = VirtualTimeScheduler.create();

        CountDownLatch latch = new CountDownLatch(1);
        Flux.interval(Duration.ofMillis(100), virtualTimeScheduler)
            .take(100)
            .flatMap(i -> cache.getToken())
            .doOnComplete(latch::countDown)
            .subscribe();

        virtualTimeScheduler.advanceTimeBy(Duration.ofSeconds(40));

        assertTrue(latch.await(30, TimeUnit.SECONDS));

        // At most 10 requests should do actual token acquisition, use 11 for safe
        assertTrue(refreshes.get() <= 11);
    }

    private Mono<AccessToken> remoteGetTokenThatExpiresSoonAsync() {
        return Mono.delay(Duration.ofMillis(1000))
            .map(l -> new Token(Integer.toString(ThreadLocalRandom.current().nextInt(100)), 0));
    }

    // First token takes latency seconds, and adds 1 sec every subsequent call
    private Mono<AccessToken> incrementalRemoteGetTokenAsync(AtomicInteger latency) {
        return Mono.delay(Duration.ofSeconds(latency.getAndIncrement()))
            .map(l -> new Token(Integer.toString(ThreadLocalRandom.current().nextInt(100))));
    }

    private static class Token extends AccessToken {
        Token(String token) {
            this(token, 5000);
        }

        Token(String token, long validityInMillis) {
            super(token, OffsetDateTime.now().plus(Duration.ofMillis(validityInMillis)));
        }
    }
}
