// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for {@link SharedRoutingMapCacheRegistry}. Each test uses a unique endpoint
 *  and releases every reference, since the registry is a process-wide singleton. */
public class SharedRoutingMapCacheRegistryTest {

    private static final String ENABLE_FLAG = "COSMOS.SHARED_PARTITION_KEY_RANGE_CACHE_ENABLED";

    private String savedFlag;

    @BeforeMethod(groups = "unit")
    public void before() {
        savedFlag = System.getProperty(ENABLE_FLAG);
        System.clearProperty(ENABLE_FLAG); // default is enabled
    }

    @AfterMethod(groups = "unit")
    public void after() {
        if (savedFlag == null) {
            System.clearProperty(ENABLE_FLAG);
        } else {
            System.setProperty(ENABLE_FLAG, savedFlag);
        }
    }

    @Test(groups = "unit")
    public void acquireReturnsSameInstanceForSameEndpoint() {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-1.documents.azure.com:443/");

        AsyncCacheNonBlocking<String, CollectionRoutingMap> a = registry.acquire(endpoint);
        AsyncCacheNonBlocking<String, CollectionRoutingMap> b = registry.acquire(endpoint);

        try {
            assertThat(a).isSameAs(b);
            assertThat(registry.referenceCount(endpoint)).isEqualTo(2);
        } finally {
            registry.release(endpoint, a);
            registry.release(endpoint, b);
        }
        assertThat(registry.referenceCount(endpoint)).isZero();
    }

    @Test(groups = "unit")
    public void acquireReturnsDifferentInstanceForDifferentEndpoints() {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI e1 = URI.create("https://test-acct-share-2a.documents.azure.com:443/");
        URI e2 = URI.create("https://test-acct-share-2b.documents.azure.com:443/");

        AsyncCacheNonBlocking<String, CollectionRoutingMap> a = registry.acquire(e1);
        AsyncCacheNonBlocking<String, CollectionRoutingMap> b = registry.acquire(e2);

        try {
            assertThat(a).isNotSameAs(b);
        } finally {
            registry.release(e1, a);
            registry.release(e2, b);
        }
    }

    @Test(groups = "unit")
    public void acquireTreatsHostCaseInsensitivelyMatchingUriEquals() {
        // URI.equals is case-insensitive on host (RFC 3986); confirm clients with
        // mixed-case host names collapse into one shared entry.
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI lower = URI.create("https://test-acct-share-case.documents.azure.com:443/");
        URI mixed = URI.create("https://Test-Acct-Share-Case.documents.azure.com:443/");

        assertThat(lower).isEqualTo(mixed);

        AsyncCacheNonBlocking<String, CollectionRoutingMap> a = registry.acquire(lower);
        AsyncCacheNonBlocking<String, CollectionRoutingMap> b = registry.acquire(mixed);

        try {
            assertThat(a)
                .as("lower-case and mixed-case host must share the same registry entry")
                .isSameAs(b);
            assertThat(registry.referenceCount(lower)).isEqualTo(2);
            assertThat(registry.referenceCount(mixed)).isEqualTo(2);
        } finally {
            registry.release(lower, a);
            registry.release(mixed, b);
        }
        assertThat(registry.referenceCount(lower)).isZero();
    }

    @Test(groups = "unit")
    public void releaseEvictsAtZeroRefcount() {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-3.documents.azure.com:443/");

        AsyncCacheNonBlocking<String, CollectionRoutingMap> a = registry.acquire(endpoint);
        AsyncCacheNonBlocking<String, CollectionRoutingMap> b = registry.acquire(endpoint);
        assertThat(registry.referenceCount(endpoint)).isEqualTo(2);

        registry.release(endpoint, a);
        assertThat(registry.referenceCount(endpoint)).isEqualTo(1);

        registry.release(endpoint, b);
        assertThat(registry.referenceCount(endpoint)).isZero();

        // After eviction, fresh acquire produces a new cache instance.
        AsyncCacheNonBlocking<String, CollectionRoutingMap> c = registry.acquire(endpoint);
        try {
            assertThat(c).isNotSameAs(a);
            assertThat(registry.referenceCount(endpoint)).isEqualTo(1);
        } finally {
            registry.release(endpoint, c);
        }
    }

    @Test(groups = "unit")
    public void releaseIsIdempotentWhenSuppliedSameCacheRepeatedly() {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-4.documents.azure.com:443/");

        AsyncCacheNonBlocking<String, CollectionRoutingMap> a = registry.acquire(endpoint);
        registry.release(endpoint, a);
        assertThat(registry.referenceCount(endpoint)).isZero();

        // Releasing a stale cache reference must not crash or drive refcount negative.
        registry.release(endpoint, a);
        assertThat(registry.referenceCount(endpoint)).isZero();
    }

    @Test(groups = "unit")
    public void releaseIsNoOpWhenCacheIsNotTheRegisteredInstance() {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-5.documents.azure.com:443/");

        AsyncCacheNonBlocking<String, CollectionRoutingMap> stale = registry.acquire(endpoint);
        registry.release(endpoint, stale);

        AsyncCacheNonBlocking<String, CollectionRoutingMap> current = registry.acquire(endpoint);
        try {
            registry.release(endpoint, stale); // stale != current → no-op
            assertThat(registry.referenceCount(endpoint)).isEqualTo(1);
        } finally {
            registry.release(endpoint, current);
        }
    }

    @Test(groups = "unit")
    public void nullEndpointReturnsIsolatedCacheAndDoesNotEnterRegistry() {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        int before = registry.registeredEndpointCount();

        AsyncCacheNonBlocking<String, CollectionRoutingMap> a = registry.acquire(null);
        AsyncCacheNonBlocking<String, CollectionRoutingMap> b = registry.acquire(null);

        assertThat(a).isNotSameAs(b);
        assertThat(registry.registeredEndpointCount()).isEqualTo(before);

        registry.release(null, a);
        registry.release(null, b);
    }

    @Test(groups = "unit")
    public void disabledFlagReturnsIsolatedCachesAndPreservesRegistryEmpty() {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-6.documents.azure.com:443/");
        int before = registry.registeredEndpointCount();

        System.setProperty(ENABLE_FLAG, "false");
        assertThat(Configs.isSharedPartitionKeyRangeCacheEnabled()).isFalse();

        AsyncCacheNonBlocking<String, CollectionRoutingMap> a = registry.acquire(endpoint);
        AsyncCacheNonBlocking<String, CollectionRoutingMap> b = registry.acquire(endpoint);

        try {
            assertThat(a).isNotSameAs(b);
            assertThat(registry.registeredEndpointCount()).isEqualTo(before);
            assertThat(registry.referenceCount(endpoint)).isZero();
        } finally {
            registry.release(endpoint, a);
            registry.release(endpoint, b);
        }
    }

    @Test(groups = "unit")
    public void concurrentAcquireAndReleaseProducesConsistentRefcount() throws Exception {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-7.documents.azure.com:443/");

        int threads = 32;
        int opsPerThread = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        try {
            for (int t = 0; t < threads; t++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        List<AsyncCacheNonBlocking<String, CollectionRoutingMap>> held = new ArrayList<>();
                        for (int i = 0; i < opsPerThread; i++) {
                            held.add(registry.acquire(endpoint));
                            if (i % 3 == 0 && !held.isEmpty()) {
                                AsyncCacheNonBlocking<String, CollectionRoutingMap> c =
                                    held.remove(held.size() - 1);
                                registry.release(endpoint, c);
                            }
                        }
                        for (AsyncCacheNonBlocking<String, CollectionRoutingMap> c : held) {
                            registry.release(endpoint, c);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            pool.shutdownNow();
        }

        assertThat(registry.referenceCount(endpoint)).isZero();
    }

    @Test(groups = "unit")
    public void referenceManagerReleasesSharedCacheWhenOwnerIsGarbageCollected() throws Exception {
        // Owner is allocated in a separate stack frame so this frame can't keep it alive;
        // ReferenceManager runs the cleanup once GC observes the owner as phantom-reachable.
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-leak-1.documents.azure.com:443/");

        acquireAndLeakOwner(registry, endpoint);
        assertThat(registry.referenceCount(endpoint)).isEqualTo(1);

        boolean released = false;
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadlineNanos) {
            System.gc();
            System.runFinalization();
            Thread.sleep(100);
            if (registry.referenceCount(endpoint) == 0) {
                released = true;
                break;
            }
        }

        assertThat(released)
            .as("ReferenceManager should release the shared cache reference after the owner is GC'd "
                + "(refcount=%d)", registry.referenceCount(endpoint))
            .isTrue();
    }

    private static void acquireAndLeakOwner(SharedRoutingMapCacheRegistry registry, URI endpoint) {
        Object owner = new Object();
        registry.acquire(endpoint, owner);
        // owner falls out of scope on return.
    }

    @Test(groups = "unit")
    public void promptCloseFulfillsHandleSoReferenceManagerCleanupIsANoop() throws Exception {
        SharedRoutingMapCacheRegistry registry = SharedRoutingMapCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-leak-2.documents.azure.com:443/");

        acquireAndPromptlyClose(registry, endpoint);
        assertThat(registry.referenceCount(endpoint)).isZero();

        // GC + cleanup-action firing must not drive the refcount negative.
        for (int i = 0; i < 5; i++) {
            System.gc();
            System.runFinalization();
            Thread.sleep(50);
        }
        assertThat(registry.referenceCount(endpoint)).isZero();
    }

    private static void acquireAndPromptlyClose(SharedRoutingMapCacheRegistry registry, URI endpoint) {
        Object owner = new Object();
        SharedRoutingMapCacheRegistry.AcquireResult result = registry.acquire(endpoint, owner);
        registry.release(endpoint, result.cache, result.releaseHandle);
    }
}
