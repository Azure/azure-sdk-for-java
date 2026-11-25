// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AiaResponseCacheTest {
    private static final X509Certificate TEST_CERTIFICATE = loadTestCertificate();
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
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

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
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

        cache.getOrLoad("url", () -> entry(certificates, loads));
        clock.set(2_001L);
        cache.getOrLoad("url", () -> entry(certificates, loads));

        assertEquals(2, loads.get());
    }

    @Test
    void lookupResultReportsSourceAndGeneration() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

        AiaResponseCache.LookupResult loaded
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(certificates, 2_000L), () -> {
            });
        AiaResponseCache.LookupResult cached
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(certificates, 2_000L), () -> {
            });

        assertEquals(AiaResponseCache.Source.LOAD, loaded.getSource());
        assertEquals(AiaResponseCache.Source.CACHE, cached.getSource());
        assertTrue(loaded.getGeneration() > 0);
        assertEquals(loaded.getGeneration(), cached.getGeneration());
        assertSame(certificates, cached.getCertificates());
    }

    @Test
    void refreshIfUnchangedSkipsLoaderWhenEntryChanged() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger refreshLoads = new AtomicInteger();
        List<X509Certificate> firstCertificates = Collections.singletonList(TEST_CERTIFICATE);
        List<X509Certificate> secondCertificates = Collections.singletonList(TEST_CERTIFICATE);

        AiaResponseCache.LookupResult first
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(firstCertificates, 2_000L), () -> {
            });
        cache.clear();
        AiaResponseCache.LookupResult second
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(secondCertificates, 2_000L), () -> {
            });

        AiaResponseCache.LookupResult refreshed = cache.refreshIfUnchanged("url", first.getGeneration(), () -> {
            refreshLoads.incrementAndGet();
            return new AiaResponseCache.Entry(firstCertificates, 2_000L);
        });

        assertEquals(0, refreshLoads.get());
        assertEquals(second.getGeneration(), refreshed.getGeneration());
        assertSame(secondCertificates, refreshed.getCertificates());
    }

    @Test
    void coalescesConcurrentForcedRefreshes() throws Exception {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        List<X509Certificate> oldCertificates = Collections.singletonList(TEST_CERTIFICATE);
        List<X509Certificate> newCertificates = Collections.singletonList(TEST_CERTIFICATE);
        AiaResponseCache.LookupResult initial
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(oldCertificates, 2_000L), () -> {
            });
        AtomicInteger refreshLoads = new AtomicInteger();
        CountDownLatch refreshStarted = new CountDownLatch(1);
        CountDownLatch releaseRefresh = new CountDownLatch(1);
        List<Future<AiaResponseCache.LookupResult>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> cache.refreshIfUnchanged("url", initial.getGeneration(), () -> {
                refreshLoads.incrementAndGet();
                refreshStarted.countDown();
                await(releaseRefresh);
                return new AiaResponseCache.Entry(newCertificates, 2_000L);
            })));
        }

        assertTrue(refreshStarted.await(5, TimeUnit.SECONDS));
        releaseRefresh.countDown();
        for (Future<AiaResponseCache.LookupResult> future : futures) {
            AiaResponseCache.LookupResult result = future.get(5, TimeUnit.SECONDS);
            assertSame(newCertificates, result.getCertificates());
        }
        assertEquals(1, refreshLoads.get());
    }

    @Test
    void lateRefreshDoesNotOverwriteNewerNormalLoad() throws Exception {
        List<String> messages = Collections.synchronizedList(new ArrayList<>());
        AiaResponseCache cache = new AiaResponseCache(128, clock::get, (message, parameters) -> messages.add(message));
        List<X509Certificate> initialCertificates = Collections.singletonList(TEST_CERTIFICATE);
        List<X509Certificate> refreshedCertificates = Collections.singletonList(TEST_CERTIFICATE);
        List<X509Certificate> loadedCertificates = Collections.singletonList(TEST_CERTIFICATE);
        AiaResponseCache.LookupResult initial
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(initialCertificates, 1_500L), () -> {
            });
        CountDownLatch refreshStarted = new CountDownLatch(1);
        CountDownLatch releaseRefresh = new CountDownLatch(1);

        Future<AiaResponseCache.LookupResult> refresh
            = executor.submit(() -> cache.refreshIfUnchanged("url", initial.getGeneration(), () -> {
                refreshStarted.countDown();
                await(releaseRefresh);
                return new AiaResponseCache.Entry(refreshedCertificates, 3_000L);
            }));

        assertTrue(refreshStarted.await(5, TimeUnit.SECONDS));
        clock.set(1_501L);
        AiaResponseCache.LookupResult loaded
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(loadedCertificates, 3_000L), () -> {
            });
        releaseRefresh.countDown();

        AiaResponseCache.LookupResult refreshResult = refresh.get(5, TimeUnit.SECONDS);
        AiaResponseCache.LookupResult cached
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(Collections.emptyList(), 3_000L), () -> {
            });

        assertSame(loadedCertificates, refreshResult.getCertificates());
        assertEquals(loaded.getGeneration(), refreshResult.getGeneration());
        assertSame(loadedCertificates, cached.getCertificates());
        assertEquals(loaded.getGeneration(), cached.getGeneration());
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Discarding completed AIA refresh")));
    }

    @Test
    void differentGenerationsDoNotShareForcedRefresh() throws Exception {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        List<X509Certificate> initialCertificates = Collections.singletonList(TEST_CERTIFICATE);
        List<X509Certificate> firstRefreshCertificates = Collections.singletonList(TEST_CERTIFICATE);
        List<X509Certificate> loadedCertificates = Collections.singletonList(TEST_CERTIFICATE);
        List<X509Certificate> secondRefreshCertificates = Collections.singletonList(TEST_CERTIFICATE);
        AiaResponseCache.LookupResult initial
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(initialCertificates, 1_500L), () -> {
            });
        CountDownLatch firstRefreshStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstRefresh = new CountDownLatch(1);

        Future<AiaResponseCache.LookupResult> firstRefresh
            = executor.submit(() -> cache.refreshIfUnchanged("url", initial.getGeneration(), () -> {
                firstRefreshStarted.countDown();
                await(releaseFirstRefresh);
                return new AiaResponseCache.Entry(firstRefreshCertificates, 3_000L);
            }));

        assertTrue(firstRefreshStarted.await(5, TimeUnit.SECONDS));
        clock.set(1_501L);
        AiaResponseCache.LookupResult loaded
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(loadedCertificates, 3_000L), () -> {
            });
        CountDownLatch secondRefreshStarted = new CountDownLatch(1);
        Future<AiaResponseCache.LookupResult> secondRefresh
            = executor.submit(() -> cache.refreshIfUnchanged("url", loaded.getGeneration(), () -> {
                secondRefreshStarted.countDown();
                return new AiaResponseCache.Entry(secondRefreshCertificates, 3_000L);
            }));

        AiaResponseCache.LookupResult secondRefreshResult;
        try {
            assertTrue(secondRefreshStarted.await(5, TimeUnit.SECONDS));
            // Publish the newer generation before releasing the older refresh. Otherwise both refreshes race to
            // complete, and the test observes scheduler order instead of the generation isolation being tested.
            secondRefreshResult = secondRefresh.get(5, TimeUnit.SECONDS);
        } finally {
            releaseFirstRefresh.countDown();
        }

        AiaResponseCache.LookupResult firstRefreshResult = firstRefresh.get(5, TimeUnit.SECONDS);
        AiaResponseCache.LookupResult cached
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(Collections.emptyList(), 3_000L), () -> {
            });

        assertSame(secondRefreshCertificates, secondRefreshResult.getCertificates());
        assertSame(secondRefreshCertificates, firstRefreshResult.getCertificates());
        assertSame(secondRefreshCertificates, cached.getCertificates());
        assertEquals(secondRefreshResult.getGeneration(), firstRefreshResult.getGeneration());
        assertEquals(secondRefreshResult.getGeneration(), cached.getGeneration());
    }

    @Test
    void negativeForcedRefreshKeepsPositiveEntry() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        List<X509Certificate> positiveCertificates = Collections.singletonList(TEST_CERTIFICATE);
        AiaResponseCache.LookupResult initial
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(positiveCertificates, 2_000L), () -> {
            });

        AiaResponseCache.LookupResult refreshed = cache.refreshIfUnchanged("url", initial.getGeneration(),
            () -> new AiaResponseCache.Entry(Collections.emptyList(), 2_000L));
        AiaResponseCache.LookupResult cached
            = cache.getOrLoadResult("url", () -> new AiaResponseCache.Entry(Collections.emptyList(), 2_000L), () -> {
            });

        assertTrue(refreshed.isNegative());
        assertSame(positiveCertificates, cached.getCertificates());
        assertEquals(initial.getGeneration(), cached.getGeneration());
    }

    @Test
    void targetSuppressionsAreIndependent() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);

        cache.suppressRefresh("url", "target-1", 1L, 1_000L);

        assertTrue(cache.isRefreshSuppressed("url", "target-1", 1L));
        assertFalse(cache.isRefreshSuppressed("url", "target-2", 1L));
    }

    @Test
    void targetSuppressionIsScopedToEntryGeneration() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);

        cache.suppressRefresh("url", "target", 1L, 1_000L);

        assertTrue(cache.isRefreshSuppressed("url", "target", 1L));
        assertFalse(cache.isRefreshSuppressed("url", "target", 2L));
    }

    @Test
    void targetSuppressionExpires() {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        cache.suppressRefresh("url", "target", 1L, 1_000L);

        clock.set(2_001L);

        assertFalse(cache.isRefreshSuppressed("url", "target", 1L));
    }

    @Test
    void reportsCacheAndSuppressionLifecycle() {
        List<String> messages = new ArrayList<>();
        AiaResponseCache cache = new AiaResponseCache(1, clock::get, (message, parameters) -> messages.add(message));
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

        AiaResponseCache.LookupResult first
            = cache.getOrLoadResult("url-1", () -> new AiaResponseCache.Entry(certificates, 2_000L), () -> {
            });
        cache.getOrLoadResult("url-1", () -> new AiaResponseCache.Entry(Collections.emptyList(), 2_000L), () -> {
        });
        AiaResponseCache.LookupResult second
            = cache.getOrLoadResult("url-2", () -> new AiaResponseCache.Entry(certificates, 2_000L), () -> {
            });
        cache.suppressRefresh("url-2", "target", second.getGeneration(), 500L);

        assertTrue(cache.isRefreshSuppressed("url-2", "target", second.getGeneration()));
        clock.set(1_501L);
        assertFalse(cache.isRefreshSuppressed("url-2", "target", second.getGeneration()));
        clock.set(2_001L);
        cache.getOrLoad("url-2", () -> new AiaResponseCache.Entry(certificates, 3_000L));

        assertTrue(first.getGeneration() > 0);
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("AIA response cache miss for URL")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Cached AIA response for URL")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Reusing the cached AIA response")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Evicted least-recently-used AIA")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Suppressed forced AIA refresh")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Expired forced AIA refresh")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Removed expired AIA response")));
    }

    @Test
    void coalescesConcurrentMissesForSameUrl() throws Exception {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch loaderStarted = new CountDownLatch(1);
        CountDownLatch releaseLoader = new CountDownLatch(1);
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);
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
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

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
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

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
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

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
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

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
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

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
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

        cache.getOrLoad("url", () -> entry(certificates, loads));
        cache.clear();
        cache.getOrLoad("url", () -> entry(certificates, loads));

        assertEquals(2, loads.get());
    }

    @Test
    void clearDuringLoadDoesNotRepopulateCache() throws Exception {
        AiaResponseCache cache = new AiaResponseCache(128, clock::get);
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch loadStarted = new CountDownLatch(1);
        CountDownLatch releaseLoad = new CountDownLatch(1);
        List<X509Certificate> certificates = Collections.singletonList(TEST_CERTIFICATE);

        Future<List<X509Certificate>> first = executor.submit(() -> cache.getOrLoad("url", () -> {
            loads.incrementAndGet();
            loadStarted.countDown();
            await(releaseLoad);
            return new AiaResponseCache.Entry(certificates, 2_000L);
        }));

        assertTrue(loadStarted.await(5, TimeUnit.SECONDS));
        cache.clear();
        releaseLoad.countDown();
        assertSame(certificates, first.get(5, TimeUnit.SECONDS));
        assertSame(certificates, cache.getOrLoad("url", () -> entry(certificates, loads)));

        assertEquals(2, loads.get());
    }

    private AiaResponseCache.Entry entry(List<X509Certificate> certificates, AtomicInteger loads) {
        loads.incrementAndGet();
        return new AiaResponseCache.Entry(certificates, 2_000L);
    }

    private static X509Certificate loadTestCertificate() {
        try (InputStream inputStream = AiaResponseCacheTest.class.getResourceAsStream("/well-known/sideload.pem")) {
            if (inputStream == null) {
                throw new IllegalStateException("Test certificate resource was not found.");
            }
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
        } catch (IOException | CertificateException e) {
            throw new IllegalStateException("Failed to load the test certificate.", e);
        }
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
