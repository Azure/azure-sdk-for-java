package com.azure.identity;

import com.azure.identity.implementation.RefreshableTokenCredential;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RefreshableTokenCredentialTests {
    private static final Random RANDOM = new Random();

    @Test
    public void testOnlyOneThreadRefreshesToken() throws Exception {
        // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
        RefreshableTokenCredential<Token> refresher = new RefreshableTokenCredential<Token>() {
            @Override
            protected String getTokenFromAuthResult(Token authResult) {
                return authResult.token();
            }

            @Override
            protected boolean isExpired(Token authResult) {
                return authResult.isExpired();
            }

            @Override
            protected Mono<Token> authenticateAsync(String resource) {
                return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
            }
        };

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong maxMillis = new AtomicLong(0);

        Flux.range(1, 10)
            .flatMap(i -> Mono.just(OffsetDateTime.now())
                // Runs refresher.getTokenAsync() on 10 different threads
                .subscribeOn(Schedulers.newParallel("pool", 10))
                .flatMap(start -> refresher.getTokenAsync(Arrays.asList("resource.default"))
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
        Assert.assertTrue(maxMillis.get() > 1000);
        Assert.assertTrue(maxMillis.get() < 2000); // Big enough for any latency, small enough to make sure no get token is called twice
    }

    @Test
    public void testLongRunningWontOverflow() throws Exception {
        // token expires on creation. Run this 100 times to simulate running the application a long time
        RefreshableTokenCredential<Token> refresher = new RefreshableTokenCredential<Token>() {
            @Override
            protected String getTokenFromAuthResult(Token authResult) {
                return authResult.token();
            }

            @Override
            protected boolean isExpired(Token authResult) {
                return authResult.isExpired();
            }

            @Override
            protected Mono<Token> authenticateAsync(String resource) {
                return remoteGetTokenThatExpiresSoonAsync(1000, 0);
            }
        };

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong refreshes = new AtomicLong(0);

        Flux.interval(Duration.ofMillis(100))
            .take(100)
            .flatMap(i -> Mono.just(OffsetDateTime.now())
                // Runs refresher.getTokenAsync() on 10 different threads
                .subscribeOn(Schedulers.newParallel("pool", 100))
                .flatMap(start -> refresher.getTokenAsync(Arrays.asList("resource.default"))
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                        if (millis > 1000) {
                            refreshes.incrementAndGet();
                        }
//                        System.out.format("Thread: %s\tDuration: %smillis%n",
//                            Thread.currentThread().getName(), Duration.between(start, OffsetDateTime.now()).toMillis());
                    })))
            .doOnComplete(latch::countDown)
            .subscribe();

        latch.await();
        // At most 10 requests should do actual token acquisition, use 11 for safe
        Assert.assertTrue(refreshes.get() <= 11);
    }

    @Test
    public void testOverflowBuffer() throws Exception {
        // Run 100 resources to make sure the buffer can handle it
        RefreshableTokenCredential<Token> refresher = new RefreshableTokenCredential<Token>() {
            @Override
            protected String getTokenFromAuthResult(Token authResult) {
                return authResult.token();
            }

            @Override
            protected boolean isExpired(Token authResult) {
                return authResult.isExpired();
            }

            @Override
            protected Mono<Token> authenticateAsync(String resource) {
                return remoteGetTokenAsync(1000);
            }
        };

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong minMillis = new AtomicLong(5000);

        // Default buffer size for token cache is 16
        Flux.range(1, 100)
            .flatMap(i -> Mono.just(OffsetDateTime.now())
                // Runs refresher.getTokenAsync() on 10 different threads
                .subscribeOn(Schedulers.newParallel("pool", 100))
                .flatMap(start -> refresher.getTokenAsync(Arrays.asList("resource" + i +".default")) // a different resource every time
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                        if (millis < minMillis.get()) {
                            minMillis.set(millis);
                        }
//                        System.out.format("Resource: %s\tDuration: %smillis%n",
//                            "resource" + i, Duration.between(start, OffsetDateTime.now()).toMillis());
                    })))
            .doOnComplete(latch::countDown)
            .subscribe();

        latch.await();
        Assert.assertTrue(minMillis.get() > 1000); // each request did a token refresh
    }

    @Test
    public void testRefreshIsCalledBeforeNew() throws Exception {
        // Token expires on acquisition so every one has to get new token. But refresh is faster
        RefreshableTokenCredential<Token> refresher = new RefreshableTokenCredential<Token>() {
            @Override
            protected String getTokenFromAuthResult(Token authResult) {
                return authResult.token();
            }

            @Override
            protected boolean isExpired(Token authResult) {
                return authResult.isExpired();
            }

            @Override
            protected Mono<Token> authenticateAsync(String resource) {
                return remoteGetTokenThatExpiresSoonAsync(2000, 0);
            }

            @Override
            protected Mono<Token> refreshAsync(Token expiredResult, String resource) {
                return remoteGetTokenThatExpiresSoonAsync(100, 0);
            }
        };

        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong maxMillis = new AtomicLong(0);

        Flux<Long> flux1 = Flux.just(-1L);
        Flux<Long> flux2 = Mono.delay(Duration.ofMillis(2500)).flatMapMany(i -> Flux.interval(Duration.ofMillis(200)));
        Flux.concat(flux1, flux2)
            .take(5)
            .flatMap(i -> Mono.just(OffsetDateTime.now())
                // Runs refresher.getTokenAsync() on 10 different threads
                .subscribeOn(Schedulers.newParallel("pool", 10))
                .flatMap(start -> refresher.getTokenAsync(Arrays.asList("resource.default"))
                    .map(t -> Duration.between(start, OffsetDateTime.now()).toMillis())
                    .doOnNext(millis -> {
                        if (i >= 0 && millis > maxMillis.get()) {
                            maxMillis.set(millis);
                        }
//                        System.out.format("Thread: %s\tDuration: %smillis%n",
//                            Thread.currentThread().getName(), Duration.between(start, OffsetDateTime.now()).toMillis());
                    })))
            .doOnComplete(latch::countDown)
            .subscribe();

        latch.await();
        Assert.assertTrue(maxMillis.get() < 1000); // no new is called
    }

    private Mono<Token> remoteGetTokenAsync(long delayInMillis) {
        return Mono.delay(Duration.ofMillis(delayInMillis))
            .map(l -> new Token(Integer.toString(RANDOM.nextInt(100))));
    }

    private Mono<Token> remoteGetTokenThatExpiresSoonAsync(long delayInMillis, long validityInMillis) {
        return Mono.delay(Duration.ofMillis(delayInMillis))
            .map(l -> new Token(Integer.toString(RANDOM.nextInt(100)), validityInMillis));
    }

    // First token takes latency seconds, and adds 1 sec every subsequent call
    private Mono<Token> incrementalRemoteGetTokenAsync(AtomicInteger latency) {
        return Mono.delay(Duration.ofSeconds(latency.getAndIncrement()))
            .map(l -> new Token(Integer.toString(RANDOM.nextInt(100))));
    }

    private static class Token {
        private String token;
        private OffsetDateTime expiry;

        String token() {
            return token;
        }

        Token(String token) {
            this(token, 5000);
        }

        Token(String token, long validityInMillis) {
            this.token = token;
            this.expiry = OffsetDateTime.now().plus(Duration.ofMillis(validityInMillis));
        }

        boolean isExpired() {
            return OffsetDateTime.now().isAfter(expiry);
        }
    }
}
