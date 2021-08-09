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

        Flux.range(1, 10)
            .flatMap(i -> Mono.just(OffsetDateTime.now())
                // Runs cache.getToken() on 10 different threads
                .publishOn(Schedulers.parallel())
                .flatMap(start -> cache.getToken()
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                        if (millis > maxMillis.get()) {
                            maxMillis.set(millis);
                        }
//                        System.out.format("Thread: %s\tDuration: %smillis%n",
//                            Thread.currentThread().getName(), Duration.between(start, OffsetDateTime.now()).toMillis());
                    })))
            .doOnComplete(latch::countDown)
            .subscribe();

        latch.await();
        long maxMs = maxMillis.get();
        Assertions.assertTrue(maxMs > 1000, () -> String.format("maxMillis was less than 1000ms. Was %d.", maxMs));

        // Big enough for any latency, small enough to make sure no get token is called twice
        Assertions.assertTrue(maxMs < 2000, () -> String.format("maxMillis was greater than 2000ms. Was %d.", maxMs));
    }

    @Test
    public void testLongRunningWontOverflow() throws Exception {
        AtomicLong refreshes = new AtomicLong(0);

        // token expires on creation. Run this 100 times to simulate running the application a long time
        SimpleTokenCache cache = new SimpleTokenCache(() -> {
            refreshes.incrementAndGet();
            return remoteGetTokenThatExpiresSoonAsync(1000, 0);
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
//                        System.out.format("Thread: %s\tDuration: %smillis%n",
//                            Thread.currentThread().getName(), Duration.between(start, OffsetDateTime.now()).toMillis());
                    })))
            .doOnComplete(latch::countDown)
            .subscribe();

        virtualTimeScheduler.advanceTimeBy(Duration.ofSeconds(40));

        latch.await();
        // At most 10 requests should do actual token acquisition, use 11 for safe
        Assertions.assertTrue(refreshes.get() <= 11);
    }

    private Mono<AccessToken> remoteGetTokenAsync(long delayInMillis) {
        return Mono.delay(Duration.ofMillis(delayInMillis))
            .map(l -> new Token(Integer.toString(RANDOM.nextInt(100))));
    }

    private Mono<AccessToken> remoteGetTokenThatExpiresSoonAsync(long delayInMillis, long validityInMillis) {
        return Mono.delay(Duration.ofMillis(delayInMillis))
            .map(l -> new Token(Integer.toString(RANDOM.nextInt(100)), validityInMillis));
    }

    // First token takes latency seconds, and adds 1 sec every subsequent call
    private Mono<AccessToken> incrementalRemoteGetTokenAsync(AtomicInteger latency) {
        return Mono.delay(Duration.ofSeconds(latency.getAndIncrement()))
            .map(l -> new Token(Integer.toString(RANDOM.nextInt(100))));
    }

    private static class Token extends AccessToken {
        private String token;
        private OffsetDateTime expiry;

        @Override
        public String getToken() {
            return token;
        }

        Token(String token) {
            this(token, 5000);
        }

        Token(String token, long validityInMillis) {
            super(token, OffsetDateTime.now().plus(Duration.ofMillis(validityInMillis)));
            this.token = token;
            this.expiry = OffsetDateTime.now().plus(Duration.ofMillis(validityInMillis));
        }

        @Override
        public OffsetDateTime getExpiresAt() {
            return expiry;
        }

        @Override
        public boolean isExpired() {
            return OffsetDateTime.now().isAfter(expiry);
        }
    }
}
