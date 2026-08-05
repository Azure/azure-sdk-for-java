// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class AiaResponseCacheTest {
    private final AtomicLong clock = new AtomicLong(1_000L);
    private final ExecutorService executor = Executors.newFixedThreadPool(16);

    @AfterEach
    void shutdownExecutor() throws InterruptedException {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void reusesSuccessfulResolutionBeforeExpiry() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));

        assertSame(certificates, cache.getOrLoad("url", () -> entry(certificates, loads)));
        assertSame(certificates, cache.getOrLoad("url", () -> entry(certificates, loads)));

        assertEquals(1, loads.get());
    }

    @Test
    void reusesNegativeResolutionBeforeExpiry() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();

        cache.getOrLoad("url", () -> entry(Collections.emptyList(), loads));
        cache.getOrLoad("url", () -> entry(Collections.emptyList(), loads));

        assertEquals(1, loads.get());
    }

    @Test
    void reloadsResolutionAfterExpiry() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));

        cache.getOrLoad("url", () -> entry(certificates, loads));
        clock.set(2_001L);
        cache.getOrLoad("url", () -> entry(certificates, loads));

        assertEquals(2, loads.get());
    }

    @Test
    void coalescesConcurrentMissesForSameUrl() throws Exception {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch loaderStarted = new CountDownLatch(1);
        CountDownLatch releaseLoader = new CountDownLatch(1);
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));
        List<Future<List<X509Certificate>>> futures = new ArrayList<>();

        for (int i = 0; i < 16; i++) {
            futures.add(executor.submit(() -> cache.getOrLoad("url", () -> {
                loads.incrementAndGet();
                loaderStarted.countDown();
                await(releaseLoader);
                return new AiaResponseCache.Entry(certificates, 2_000L);
            })));
        }

        assertTrue(loaderStarted.await(5, TimeUnit.SECONDS));
        releaseLoader.countDown();
        for (Future<List<X509Certificate>> future : futures) {
            assertSame(certificates, future.get(5, TimeUnit.SECONDS));
        }
        assertEquals(1, loads.get());
    }

    @Test
    void allowsDifferentUrlsToLoadConcurrently() throws Exception {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        CountDownLatch bothLoadersStarted = new CountDownLatch(2);
        CountDownLatch releaseLoaders = new CountDownLatch(1);
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));

        Future<List<X509Certificate>> first = executor.submit(() -> cache.getOrLoad("url-1", () -> {
            bothLoadersStarted.countDown();
            await(releaseLoaders);
            return new AiaResponseCache.Entry(certificates, 2_000L);
        }));
        Future<List<X509Certificate>> second = executor.submit(() -> cache.getOrLoad("url-2", () -> {
            bothLoadersStarted.countDown();
            await(releaseLoaders);
            return new AiaResponseCache.Entry(certificates, 2_000L);
        }));

        assertTrue(bothLoadersStarted.await(5, TimeUnit.SECONDS));
        releaseLoaders.countDown();
        assertSame(certificates, first.get(5, TimeUnit.SECONDS));
        assertSame(certificates, second.get(5, TimeUnit.SECONDS));
    }

    @Test
    void loaderFailureDoesNotLeaveInFlightEntry() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));

        CompletionException exception = assertThrows(CompletionException.class, () -> cache.getOrLoad("url", () -> {
            loads.incrementAndGet();
            throw new IllegalStateException("load failed");
        }));
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertSame(certificates, cache.getOrLoad("url", () -> entry(certificates, loads)));

        assertEquals(2, loads.get());
    }

    @Test
    void loaderErrorPropagatesWithoutLeavingInFlightEntry() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));

        AssertionError error = assertThrows(AssertionError.class, () -> cache.getOrLoad("url", () -> {
            loads.incrementAndGet();
            throw new AssertionError("load failed");
        }));
        assertEquals("load failed", error.getMessage());
        assertSame(certificates, cache.getOrLoad("url", () -> entry(certificates, loads)));

        assertEquals(2, loads.get());
    }

    @Test
    void evictsOnlyLeastRecentlyUsedEntry() {
        AiaResponseCache cache = new AiaResponseCache(2, clock::get);
        AtomicInteger loads = new AtomicInteger();
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));

        cache.getOrLoad("url-1", () -> entry(certificates, loads));
        cache.getOrLoad("url-2", () -> entry(certificates, loads));
        cache.getOrLoad("url-1", () -> entry(certificates, loads));
        cache.getOrLoad("url-3", () -> entry(certificates, loads));
        cache.getOrLoad("url-1", () -> entry(certificates, loads));
        cache.getOrLoad("url-2", () -> entry(certificates, loads));

        assertEquals(4, loads.get());
        assertEquals(2, cache.size());
    }

    @Test
    void removesExpiredEntriesBeforeLruEviction() {
        AiaResponseCache cache = new AiaResponseCache(2, clock::get);
        AtomicInteger loads = new AtomicInteger();
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));

        cache.getOrLoad("expired", () -> {
            loads.incrementAndGet();
            return new AiaResponseCache.Entry(certificates, 1_500L);
        });
        cache.getOrLoad("fresh", () -> {
            loads.incrementAndGet();
            return new AiaResponseCache.Entry(certificates, 3_000L);
        });
        clock.set(2_000L);
        cache.getOrLoad("new", () -> {
            loads.incrementAndGet();
            return new AiaResponseCache.Entry(certificates, 3_000L);
        });
        cache.getOrLoad("fresh", () -> entry(certificates, loads));
        cache.getOrLoad("expired", () -> {
            loads.incrementAndGet();
            return new AiaResponseCache.Entry(certificates, 3_000L);
        });

        assertEquals(4, loads.get());
        assertEquals(2, cache.size());
    }

    @Test
    void clearRemovesCachedEntries() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();
        List<X509Certificate> certificates = Collections.singletonList(mock(X509Certificate.class));

        cache.getOrLoad("url", () -> entry(certificates, loads));
        cache.clear();
        cache.getOrLoad("url", () -> entry(certificates, loads));

        assertEquals(2, loads.get());
    }

    private AiaResponseCache.Entry entry(List<X509Certificate> certificates, AtomicInteger loads) {
        loads.incrementAndGet();
        return new AiaResponseCache.Entry(certificates, 2_000L);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test latch");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for test latch", e);
        }
    }
}
