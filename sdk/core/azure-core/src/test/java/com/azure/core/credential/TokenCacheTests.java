// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
                .subscribeOn(Schedulers.newParallel("pool", 10))
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
        Assertions.assertTrue(maxMillis.get() > 1000);
        Assertions.assertTrue(maxMillis.get() < 2000); // Big enough for any latency, small enough to make sure no get token is called twice
    }

    @Test
    public void testMultipleThreadsWaitForTimeout() throws Exception {
        AtomicLong refreshes = new AtomicLong(0);

        // token expires on creation. Run this 100 times to simulate running the application a long time
        SimpleTokenCache cache = new SimpleTokenCache(() -> {
            refreshes.incrementAndGet();
            return remoteGetTokenThatExpiresSoonAsync(1000, 0);
        }, new TokenRefreshOptions().setTokenRefreshRetryTimeout(Duration.ZERO));

        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofMillis(100))
            .take(100)
            .flatMap(i -> Mono.just(OffsetDateTime.now())
                // Runs cache.getToken() on 10 different threads
                .subscribeOn(Schedulers.newParallel("pool", 100))
                .flatMap(start -> cache.getToken()
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                    })))
            .doOnComplete(latch::countDown)
            .subscribe();

        latch.await();
        Assertions.assertTrue(refreshes.get() <= 11);
    }

    @Test
    public void testProactiveRefreshBeforeExpiry() throws Exception {
        AtomicInteger latency = new AtomicInteger(1);
        SimpleTokenCache cache = new SimpleTokenCache(
            () -> remoteGetTokenThatExpiresSoonAsync(1000 * latency.getAndIncrement(), 60 * 1000),
            new TokenRefreshOptions().setTokenRefreshOffset(Duration.ofSeconds(58)).setTokenRefreshRetryTimeout(Duration.ofSeconds(1)));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong maxMillis = new AtomicLong(0);

        Flux.interval(Duration.ofSeconds(1))
            .take(5)
            .flatMap(i -> {
                OffsetDateTime start = OffsetDateTime.now();
                return cache.getToken()
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                        if (millis > maxMillis.get()) {
                            maxMillis.set(millis);
                        }
                    });
            }).doOnComplete(latch::countDown)
            .subscribe();

        latch.await();
        Assertions.assertTrue(maxMillis.get() >= 2000);
        Assertions.assertTrue(maxMillis.get() < 3000); // Big enough for any latency, small enough to make sure no get token is called twice
    }

    @Test
    public void testRefreshAfterExpiry() throws Exception {
        AtomicInteger latency = new AtomicInteger(1);
        SimpleTokenCache cache = new SimpleTokenCache(
            () -> remoteGetTokenThatExpiresSoonAsync(1000 * latency.getAndIncrement(), 1000),
            new TokenRefreshOptions().setTokenRefreshOffset(Duration.ofSeconds(1)).setTokenRefreshRetryTimeout(Duration.ofSeconds(5)));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong maxMillis = new AtomicLong(0);

        Flux.interval(Duration.ofSeconds(1))
            .take(4)
            .flatMap(i -> {
                OffsetDateTime start = OffsetDateTime.now();
                return cache.getToken()
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                        if (millis > maxMillis.get()) {
                            maxMillis.set(millis);
                        }
                    });
            }).doOnComplete(latch::countDown)
            .subscribe();

        latch.await();
        System.out.println("MaxMillis:" + maxMillis.get());
        Assertions.assertTrue(maxMillis.get() >= 5000);
    }

    @Test
    public void testProactiveRefreshError() throws Exception {
        AtomicInteger latency = new AtomicInteger(1);
        AtomicInteger tryCount = new AtomicInteger(0);
        SimpleTokenCache cache = new SimpleTokenCache(
            () -> remoteGetTokenWithPersistentError(1000 * latency.getAndIncrement(), 3 * 1000, 2, tryCount),
            new TokenRefreshOptions().setTokenRefreshOffset(Duration.ofSeconds(2)).setTokenRefreshRetryTimeout(Duration.ofSeconds(1)));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong maxMillis = new AtomicLong(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Flux.interval(Duration.ofSeconds(1))
            .take(6)
            .flatMap(i -> {
                OffsetDateTime start = OffsetDateTime.now();
                return cache.getToken()
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                        if (millis > maxMillis.get()) {
                            maxMillis.set(millis);
                        }
                    })
                    .doOnError(t -> errorCount.incrementAndGet());
            }).doOnTerminate(latch::countDown)
            .subscribe();

        latch.await();
        Assertions.assertTrue(maxMillis.get() >= 1000);
        Assertions.assertTrue(maxMillis.get() < 2000); // Big enough for any latency, small enough to make sure no get token is called twice
        Assertions.assertEquals(1, errorCount.get()); // Only the error after expiresAt will be propagated
    }

    @Test
    public void testProactiveRefreshErrorTimeout() throws Exception {
        AtomicInteger latency = new AtomicInteger(1);
        AtomicInteger tryCount = new AtomicInteger(0);
        SimpleTokenCache cache = new SimpleTokenCache(
            () -> remoteGetTokenWithTemporaryError(1000 * latency.getAndIncrement(), 4 * 1000, 2, tryCount),
            new TokenRefreshOptions().setTokenRefreshOffset(Duration.ofSeconds(2)).setTokenRefreshRetryTimeout(Duration.ofSeconds(1)));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong maxMillis = new AtomicLong(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Flux.interval(Duration.ofSeconds(1))
            .take(8)
            .flatMap(i -> {
                OffsetDateTime start = OffsetDateTime.now();
                return cache.getToken()
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                        if (millis > maxMillis.get()) {
                            maxMillis.set(millis);
                        }
                    })
                    .doOnError(t -> errorCount.incrementAndGet());
            }).doOnTerminate(latch::countDown)
            .subscribe();

        latch.await();
        Assertions.assertTrue(maxMillis.get() >= 3000);
        Assertions.assertEquals(0, errorCount.get()); // Only the error after expiresAt will be propagated
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

    private Mono<AccessToken> remoteGetTokenWithTemporaryError(long delayInMillis, long validityInMillis, int errorAt, AtomicInteger tryCount) {
        if (tryCount.incrementAndGet() == errorAt) {
            return Mono.error(new RuntimeException("Expected error"));
        } else {
            return Mono.delay(Duration.ofMillis(delayInMillis))
                .map(l -> new Token(Integer.toString(RANDOM.nextInt(100)), validityInMillis));
        }
    }

    private Mono<AccessToken> remoteGetTokenWithPersistentError(long delayInMillis, long validityInMillis, int errorAfter, AtomicInteger tryCount) {
        if (tryCount.incrementAndGet() >= errorAfter) {
            return Mono.error(new RuntimeException("Expected error"));
        } else {
            return Mono.delay(Duration.ofMillis(delayInMillis))
                .map(l -> new Token(Integer.toString(RANDOM.nextInt(100)), validityInMillis));
        }
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
