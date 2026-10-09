// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.caches.SharedPartitionKeyRangeCacheRegistry.AcquireResult;
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

/** Unit tests for {@link SharedPartitionKeyRangeCacheRegistry}. Each test uses a unique endpoint
 *  and releases every reference, since the registry is a process-wide singleton. */
public class SharedPartitionKeyRangeCacheRegistryTest {

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
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-1.documents.azure.com:443/");

        AcquireResult ra = registry.acquire(endpoint, new Object());
        AcquireResult rb = registry.acquire(endpoint, new Object());

        try {
            assertThat(ra.cache).isSameAs(rb.cache);
            assertThat(registry.referenceCount(endpoint)).isEqualTo(2);
        } finally {
            registry.release(endpoint, ra.cache, ra.releaseHandle);
            registry.release(endpoint, rb.cache, rb.releaseHandle);
        }
        assertThat(registry.referenceCount(endpoint)).isZero();
    }

    @Test(groups = "unit")
    public void acquireReturnsDifferentInstanceForDifferentEndpoints() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        URI e1 = URI.create("https://test-acct-share-2a.documents.azure.com:443/");
        URI e2 = URI.create("https://test-acct-share-2b.documents.azure.com:443/");

        AcquireResult ra = registry.acquire(e1, new Object());
        AcquireResult rb = registry.acquire(e2, new Object());

        try {
            assertThat(ra.cache).isNotSameAs(rb.cache);
        } finally {
            registry.release(e1, ra.cache, ra.releaseHandle);
            registry.release(e2, rb.cache, rb.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void acquireTreatsHostCaseInsensitivelyMatchingUriEquals() {
        // URI.equals is case-insensitive on host (RFC 3986); confirm clients with
        // mixed-case host names collapse into one shared entry.
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        URI lower = URI.create("https://test-acct-share-case.documents.azure.com:443/");
        URI mixed = URI.create("https://Test-Acct-Share-Case.documents.azure.com:443/");

        assertThat(lower).isEqualTo(mixed);

        AcquireResult ra = registry.acquire(lower, new Object());
        AcquireResult rb = registry.acquire(mixed, new Object());

        try {
            assertThat(ra.cache)
                .as("lower-case and mixed-case host must share the same registry entry")
                .isSameAs(rb.cache);
            assertThat(registry.referenceCount(lower)).isEqualTo(2);
            assertThat(registry.referenceCount(mixed)).isEqualTo(2);
        } finally {
            registry.release(lower, ra.cache, ra.releaseHandle);
            registry.release(mixed, rb.cache, rb.releaseHandle);
        }
        assertThat(registry.referenceCount(lower)).isZero();
    }

    @Test(groups = "unit")
    public void regionalAndGlobalEndpointsDoNotShareStorage() {
        // Documents the chosen scope: the registry keys on the URI as configured on the
        // CosmosClientBuilder. A client configured with the global endpoint and a client
        // configured with a regional endpoint of the same logical account use distinct
        // entries. Sharing across endpoints would require canonicalising the account id
        // returned from regional endpoints (which embeds a service-normalised region
        // suffix that cannot be reliably reversed) and is deliberately not attempted.
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        URI global = URI.create("https://contoso.documents.azure.com:443/");
        URI regional = URI.create("https://contoso-westus.documents.azure.com:443/");

        AcquireResult ra = registry.acquire(global, new Object());
        AcquireResult rb = registry.acquire(regional, new Object());

        try {
            assertThat(ra.cache)
                .as("global and regional endpoint URIs use separate registry entries")
                .isNotSameAs(rb.cache);
            assertThat(registry.referenceCount(global)).isEqualTo(1);
            assertThat(registry.referenceCount(regional)).isEqualTo(1);
        } finally {
            registry.release(global, ra.cache, ra.releaseHandle);
            registry.release(regional, rb.cache, rb.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void releaseEvictsAtZeroRefcount() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-3.documents.azure.com:443/");

        AcquireResult ra = registry.acquire(endpoint, new Object());
        AcquireResult rb = registry.acquire(endpoint, new Object());
        assertThat(registry.referenceCount(endpoint)).isEqualTo(2);

        registry.release(endpoint, ra.cache, ra.releaseHandle);
        assertThat(registry.referenceCount(endpoint)).isEqualTo(1);

        registry.release(endpoint, rb.cache, rb.releaseHandle);
        assertThat(registry.referenceCount(endpoint)).isZero();

        // After eviction, fresh acquire produces a new cache instance.
        AcquireResult rc = registry.acquire(endpoint, new Object());
        try {
            assertThat(rc.cache).isNotSameAs(ra.cache);
            assertThat(registry.referenceCount(endpoint)).isEqualTo(1);
        } finally {
            registry.release(endpoint, rc.cache, rc.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void releaseIsIdempotentWhenSuppliedSameCacheRepeatedly() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-4.documents.azure.com:443/");

        AcquireResult ra = registry.acquire(endpoint, new Object());
        registry.release(endpoint, ra.cache, ra.releaseHandle);
        assertThat(registry.referenceCount(endpoint)).isZero();

        // Releasing a stale cache reference must not crash or drive refcount negative.
        registry.release(endpoint, ra.cache, ra.releaseHandle);
        assertThat(registry.referenceCount(endpoint)).isZero();
    }

    @Test(groups = "unit")
    public void releaseIsNoOpWhenCacheIsNotTheRegisteredInstance() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-5.documents.azure.com:443/");

        AcquireResult stale = registry.acquire(endpoint, new Object());
        registry.release(endpoint, stale.cache, stale.releaseHandle);

        AcquireResult current = registry.acquire(endpoint, new Object());
        try {
            registry.release(endpoint, stale.cache, null); // stale != current → no-op
            assertThat(registry.referenceCount(endpoint)).isEqualTo(1);
        } finally {
            registry.release(endpoint, current.cache, current.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void nullEndpointReturnsIsolatedCacheAndDoesNotEnterRegistry() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        int before = registry.registeredEndpointCount();

        AcquireResult ra = registry.acquire(null, new Object());
        AcquireResult rb = registry.acquire(null, new Object());

        assertThat(ra.cache).isNotSameAs(rb.cache);
        assertThat(ra.releaseHandle).isNull();
        assertThat(rb.releaseHandle).isNull();
        assertThat(registry.registeredEndpointCount()).isEqualTo(before);

        registry.release(null, ra.cache, ra.releaseHandle);
        registry.release(null, rb.cache, rb.releaseHandle);
    }

    @Test(groups = "unit")
    public void disabledFlagReturnsIsolatedCachesAndPreservesRegistryEmpty() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        URI endpoint = URI.create("https://test-acct-share-6.documents.azure.com:443/");
        int before = registry.registeredEndpointCount();

        System.setProperty(ENABLE_FLAG, "false");
        assertThat(Configs.isSharedPartitionKeyRangeCacheEnabled()).isFalse();

        AcquireResult ra = registry.acquire(endpoint, new Object());
        AcquireResult rb = registry.acquire(endpoint, new Object());

        try {
            assertThat(ra.cache).isNotSameAs(rb.cache);
            assertThat(ra.releaseHandle).isNull();
            assertThat(registry.registeredEndpointCount()).isEqualTo(before);
            assertThat(registry.referenceCount(endpoint)).isZero();
        } finally {
            registry.release(endpoint, ra.cache, ra.releaseHandle);
            registry.release(endpoint, rb.cache, rb.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void concurrentAcquireAndReleaseProducesConsistentRefcount() throws Exception {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
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
                        List<AcquireResult> held = new ArrayList<>();
                        for (int i = 0; i < opsPerThread; i++) {
                            held.add(registry.acquire(endpoint, new Object()));
                            if (i % 3 == 0 && !held.isEmpty()) {
                                AcquireResult r = held.remove(held.size() - 1);
                                registry.release(endpoint, r.cache, r.releaseHandle);
                            }
                        }
                        for (AcquireResult r : held) {
                            registry.release(endpoint, r.cache, r.releaseHandle);
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
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
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

    private static void acquireAndLeakOwner(SharedPartitionKeyRangeCacheRegistry registry, URI endpoint) {
        Object owner = new Object();
        registry.acquire(endpoint, owner);
        // owner falls out of scope on return.
    }

    @Test(groups = "unit")
    public void promptCloseFulfillsHandleSoReferenceManagerCleanupIsANoop() throws Exception {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
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

    private static void acquireAndPromptlyClose(SharedPartitionKeyRangeCacheRegistry registry, URI endpoint) {
        Object owner = new Object();
        AcquireResult result = registry.acquire(endpoint, owner);
        registry.release(endpoint, result.cache, result.releaseHandle);
    }
}
