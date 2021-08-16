// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TokenCacheTests {
    private static final Random RANDOM = new Random();

    @Test
    public void testOnlyOneThreadRefreshesToken() throws Exception {
        // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
        SimpleTokenCache cache = new SimpleTokenCache(() -> incrementalRemoteGetTokenAsync(new AtomicInteger(1)));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong maxMillis = new AtomicLong(0);

        Mono.fromCallable(OffsetDateTime::now)
            .repeat(10)
            .parallel(10)
            // Runs cache.getToken() on 10 different threads
            .runOn(Schedulers.boundedElastic())
            .flatMap(start -> cache.getToken()
                .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                .doOnNext(millis -> {
                    if (millis > maxMillis.get()) {
                        maxMillis.set(millis);
                    }
                }))
            .doOnComplete(latch::countDown)
            .subscribe();

        latch.await();
        long maxMs = maxMillis.get();
        Assertions.assertTrue(maxMs >= 950, () -> "maxMillis was less than 950ms. Was " + maxMs + "ms.");

        // Big enough for any latency, small enough to make sure no get token is called twice
        Assertions.assertTrue(maxMs <= 2000, () -> "maxMillis was greater than 2000ms. Was " + maxMs + "ms.");
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
            .flatMap(i -> Mono.just(OffsetDateTime.now())
                // Runs cache.getToken() on 10 different threads
                .subscribeOn(Schedulers.parallel())
                .flatMap(start -> cache.getToken()
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                    })))
            .doOnComplete(latch::countDown)
            .subscribe();

        virtualTimeScheduler.advanceTimeBy(Duration.ofSeconds(40));

        latch.await();
        // At most 10 requests should do actual token acquisition, use 11 for safe
        Assertions.assertTrue(refreshes.get() <= 11);
    }

    private Mono<AccessToken> remoteGetTokenThatExpiresSoonAsync() {
        return Mono.delay(Duration.ofMillis(1000)).map(l -> new Token(Integer.toString(RANDOM.nextInt(100)), 0));
    }

    // First token takes latency seconds, and adds 1 sec every subsequent call
    private Mono<AccessToken> incrementalRemoteGetTokenAsync(AtomicInteger latency) {
        return Mono.delay(Duration.ofSeconds(latency.getAndIncrement()))
            .map(l -> new Token(Integer.toString(RANDOM.nextInt(100))));
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
