// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.credential;

import com.azure.core.v2.implementation.AccessTokenCache;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

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
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenCacheTests {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    @Test
    public void testOnlyOneThreadRefreshesToken() {
        AtomicLong refreshes = new AtomicLong(0);

        // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
        SimpleTokenCache cache = new SimpleTokenCache(() -> {
            refreshes.incrementAndGet();
            return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
        });

        StepVerifier.create(Flux.range(1, 10)
            .flatMap(ignored -> OffsetDateTime.now()))
            .parallel(10)
            // Runs cache.getToken() on 10 different threads
            .runOn(Schedulers.boundedElastic())
            .flatMap(start -> cache.getToken())
            .then()).expectComplete().verify(DEFAULT_TIMEOUT);

        // Ensure that only one refresh attempt is made.
        assertEquals(1, refreshes.get());
    }

    @Test
    public void testOnlyOneAsyncThreadRefreshesToken() {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
        };

        // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
        AccessTokenCache cache = new AccessTokenCache(dummyCred);

        StepVerifier.create(Flux.range(1, 10)
            .flatMap(ignored -> OffsetDateTime.now()))
            .parallel(10)
            // Runs cache.getToken() on 10 different threads
            .runOn(Schedulers.boundedElastic())
            .flatMap(start -> cache.getToken(new TokenRequestContext(), false))
            .then()).expectComplete().verify(DEFAULT_TIMEOUT);

        // Ensure that only one refresh attempt is made.
        assertEquals(1, refreshes.get());
    }

    @Test
    public void testEachAsyncThreadRefreshesToken() {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
            return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
        };
        AtomicInteger atomicInteger = new AtomicInteger(0);

        AccessTokenCache cache = new AccessTokenCache(dummyCred);

        StepVerifier.create(Flux.range(1, 5)
            .flatMap(ignored -> OffsetDateTime.now()))
            .parallel(5)
            // Runs cache.getToken() on 5 different threads
            .runOn(Schedulers.boundedElastic())
            .flatMap(start -> cache.getToken(
                new TokenRequestContext().addScopes("test" + atomicInteger.incrementAndGet() + "/.default"), true))
            .then()).expectComplete().verify(DEFAULT_TIMEOUT);

        // Ensure that refresh attempts are made.
        assertEquals(5, refreshes.get());
    }

    @Test
    public void testEachSyncThreadRefreshesToken() {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
            return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
        };

        AccessTokenCache cache = new AccessTokenCache(dummyCred);

        IntStream.range(0, 5).parallel().flatMap(integer -> {
            cache.getTokenSync(new TokenRequestContext().addScopes("test" + integer + "/.default"), true);
            return IntStream.of(integer);
        }).forEach(ignored -> {
        });

        // Ensure that refresh attempts are made.
        assertEquals(5, refreshes.get());
    }

    @Test
    public void testOnlyOneSyncThreadRefreshesToken() {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
        };

        // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
        AccessTokenCache cache = new AccessTokenCache(dummyCred);

        IntStream.range(1, 10).parallel().flatMap(integer -> {
            cache.getTokenSync(new TokenRequestContext(), false);
            return IntStream.of(integer);
        }).forEach(ignored -> {
        });

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

        assertTrue(latch.await(60, TimeUnit.SECONDS));

        // At most 10 requests should do actual token acquisition, use 11 for safe
        assertTrue(refreshes.get() <= 11);
    }

    private AccessToken> remoteGetTokenThatExpiresSoonAsync() {
        return Mono.delay(Duration.ofMillis(1000))
            .map(l -> new Token(Integer.toString(ThreadLocalRandom.current().nextInt(100)), 0));
    }

    // First token takes latency seconds, and adds 1 sec every subsequent call
    private AccessToken> incrementalRemoteGetTokenAsync(AtomicInteger latency) {
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
