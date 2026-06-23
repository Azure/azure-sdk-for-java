// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.caches.SharedPartitionKeyRangeCacheRegistry.AcquireResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for {@link SharedPartitionKeyRangeCacheRegistry}. Each test uses a unique account id
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
    public void acquireReturnsSameInstanceForSameAccount() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String accountId = "test-acct-share-1";

        AcquireResult ra = registry.acquire(accountId, new Object());
        AcquireResult rb = registry.acquire(accountId, new Object());

        try {
            assertThat(ra.cache).isSameAs(rb.cache);
            assertThat(registry.referenceCount(accountId)).isEqualTo(2);
        } finally {
            registry.release(accountId, ra.cache, ra.releaseHandle);
            registry.release(accountId, rb.cache, rb.releaseHandle);
        }
        assertThat(registry.referenceCount(accountId)).isZero();
    }

    @Test(groups = "unit")
    public void acquireReturnsDifferentInstanceForDifferentAccounts() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String a1 = "test-acct-share-2a";
        String a2 = "test-acct-share-2b";

        AcquireResult ra = registry.acquire(a1, new Object());
        AcquireResult rb = registry.acquire(a2, new Object());

        try {
            assertThat(ra.cache).isNotSameAs(rb.cache);
        } finally {
            registry.release(a1, ra.cache, ra.releaseHandle);
            registry.release(a2, rb.cache, rb.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void releaseEvictsAtZeroRefcount() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String accountId = "test-acct-share-3";

        AcquireResult ra = registry.acquire(accountId, new Object());
        AcquireResult rb = registry.acquire(accountId, new Object());
        assertThat(registry.referenceCount(accountId)).isEqualTo(2);

        registry.release(accountId, ra.cache, ra.releaseHandle);
        assertThat(registry.referenceCount(accountId)).isEqualTo(1);

        registry.release(accountId, rb.cache, rb.releaseHandle);
        assertThat(registry.referenceCount(accountId)).isZero();

        // After eviction, fresh acquire produces a new cache instance.
        AcquireResult rc = registry.acquire(accountId, new Object());
        try {
            assertThat(rc.cache).isNotSameAs(ra.cache);
            assertThat(registry.referenceCount(accountId)).isEqualTo(1);
        } finally {
            registry.release(accountId, rc.cache, rc.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void releaseIsIdempotentWhenSuppliedSameCacheRepeatedly() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String accountId = "test-acct-share-4";

        AcquireResult ra = registry.acquire(accountId, new Object());
        registry.release(accountId, ra.cache, ra.releaseHandle);
        assertThat(registry.referenceCount(accountId)).isZero();

        // Releasing a stale cache reference must not crash or drive refcount negative.
        registry.release(accountId, ra.cache, ra.releaseHandle);
        assertThat(registry.referenceCount(accountId)).isZero();
    }

    @Test(groups = "unit")
    public void releaseIsNoOpWhenCacheIsNotTheRegisteredInstance() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String accountId = "test-acct-share-5";

        AcquireResult stale = registry.acquire(accountId, new Object());
        registry.release(accountId, stale.cache, stale.releaseHandle);

        AcquireResult current = registry.acquire(accountId, new Object());
        try {
            registry.release(accountId, stale.cache, null); // stale != current → no-op
            assertThat(registry.referenceCount(accountId)).isEqualTo(1);
        } finally {
            registry.release(accountId, current.cache, current.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void nullAccountIdReturnsIsolatedCacheAndDoesNotEnterRegistry() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        int before = registry.registeredAccountCount();

        AcquireResult ra = registry.acquire(null, new Object());
        AcquireResult rb = registry.acquire(null, new Object());

        assertThat(ra.cache).isNotSameAs(rb.cache);
        assertThat(ra.releaseHandle).isNull();
        assertThat(rb.releaseHandle).isNull();
        assertThat(registry.registeredAccountCount()).isEqualTo(before);

        registry.release(null, ra.cache, ra.releaseHandle);
        registry.release(null, rb.cache, rb.releaseHandle);
    }

    @Test(groups = "unit")
    public void emptyAccountIdReturnsIsolatedCacheAndDoesNotEnterRegistry() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        int before = registry.registeredAccountCount();

        AcquireResult ra = registry.acquire("", new Object());
        AcquireResult rb = registry.acquire("", new Object());

        try {
            assertThat(ra.cache).isNotSameAs(rb.cache);
            assertThat(ra.releaseHandle).isNull();
            assertThat(registry.registeredAccountCount()).isEqualTo(before);
        } finally {
            registry.release("", ra.cache, ra.releaseHandle);
            registry.release("", rb.cache, rb.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void disabledFlagReturnsIsolatedCachesAndPreservesRegistryEmpty() {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String accountId = "test-acct-share-6";
        int before = registry.registeredAccountCount();

        System.setProperty(ENABLE_FLAG, "false");
        assertThat(Configs.isSharedPartitionKeyRangeCacheEnabled()).isFalse();

        AcquireResult ra = registry.acquire(accountId, new Object());
        AcquireResult rb = registry.acquire(accountId, new Object());

        try {
            assertThat(ra.cache).isNotSameAs(rb.cache);
            assertThat(ra.releaseHandle).isNull();
            assertThat(registry.registeredAccountCount()).isEqualTo(before);
            assertThat(registry.referenceCount(accountId)).isZero();
        } finally {
            registry.release(accountId, ra.cache, ra.releaseHandle);
            registry.release(accountId, rb.cache, rb.releaseHandle);
        }
    }

    @Test(groups = "unit")
    public void concurrentAcquireAndReleaseProducesConsistentRefcount() throws Exception {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String accountId = "test-acct-share-7";

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
                            held.add(registry.acquire(accountId, new Object()));
                            if (i % 3 == 0 && !held.isEmpty()) {
                                AcquireResult r = held.remove(held.size() - 1);
                                registry.release(accountId, r.cache, r.releaseHandle);
                            }
                        }
                        for (AcquireResult r : held) {
                            registry.release(accountId, r.cache, r.releaseHandle);
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

        assertThat(registry.referenceCount(accountId)).isZero();
    }

    @Test(groups = "unit")
    public void referenceManagerReleasesSharedCacheWhenOwnerIsGarbageCollected() throws Exception {
        // Owner is allocated in a separate stack frame so this frame can't keep it alive;
        // ReferenceManager runs the cleanup once GC observes the owner as phantom-reachable.
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String accountId = "test-acct-leak-1";

        acquireAndLeakOwner(registry, accountId);
        assertThat(registry.referenceCount(accountId)).isEqualTo(1);

        boolean released = false;
        long deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadlineNanos) {
            System.gc();
            System.runFinalization();
            Thread.sleep(100);
            if (registry.referenceCount(accountId) == 0) {
                released = true;
                break;
            }
        }

        assertThat(released)
            .as("ReferenceManager should release the shared cache reference after the owner is GC'd "
                + "(refcount=%d)", registry.referenceCount(accountId))
            .isTrue();
    }

    private static void acquireAndLeakOwner(SharedPartitionKeyRangeCacheRegistry registry, String accountId) {
        Object owner = new Object();
        registry.acquire(accountId, owner);
        // owner falls out of scope on return.
    }

    @Test(groups = "unit")
    public void promptCloseFulfillsHandleSoReferenceManagerCleanupIsANoop() throws Exception {
        SharedPartitionKeyRangeCacheRegistry registry = SharedPartitionKeyRangeCacheRegistry.getInstance();
        String accountId = "test-acct-leak-2";

        acquireAndPromptlyClose(registry, accountId);
        assertThat(registry.referenceCount(accountId)).isZero();

        // GC + cleanup-action firing must not drive the refcount negative.
        for (int i = 0; i < 5; i++) {
            System.gc();
            System.runFinalization();
            Thread.sleep(50);
        }
        assertThat(registry.referenceCount(accountId)).isZero();
    }

    private static void acquireAndPromptlyClose(SharedPartitionKeyRangeCacheRegistry registry, String accountId) {
        Object owner = new Object();
        AcquireResult result = registry.acquire(accountId, owner);
        registry.release(accountId, result.cache, result.releaseHandle);
    }

    @Test(groups = "unit")
    public void canonicalAccountIdHandlesNullAndEmptyInputs() {
        assertThat(SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(null)).isNull();

        DatabaseAccount emptyAccount = new DatabaseAccount(
            "{\"id\":\"\",\"writableLocations\":[],\"readableLocations\":[]}");
        assertThat(SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(emptyAccount)).isEmpty();
    }

    @Test(groups = "unit")
    public void canonicalAccountIdPassesThroughGlobalEndpointId() {
        // Global endpoint returns the bare account id; canonicalization must not alter it.
        DatabaseAccount account = new DatabaseAccount(
            "{\"id\":\"contoso\","
                + "\"writableLocations\":[{\"name\":\"East US\","
                + "\"databaseAccountEndpoint\":\"https://contoso-eastus.documents.azure.com:443/\"}],"
                + "\"readableLocations\":[{\"name\":\"East US\","
                + "\"databaseAccountEndpoint\":\"https://contoso-eastus.documents.azure.com:443/\"}]}");
        assertThat(SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(account))
            .isEqualTo("contoso");
    }

    @Test(groups = "unit")
    public void canonicalAccountIdStripsRegionSuffixFromRegionalEndpointId() {
        // Regional endpoint returns "<global>-<normalize(region)>"; canonicalization must
        // strip the region suffix so global and regional endpoints map to the same key.
        DatabaseAccount account = new DatabaseAccount(
            "{\"id\":\"contoso-westcentralus\","
                + "\"writableLocations\":[{\"name\":\"West Central US\","
                + "\"databaseAccountEndpoint\":\"https://contoso-westcentralus.documents.azure.com:443/\"}],"
                + "\"readableLocations\":[{\"name\":\"West Central US\","
                + "\"databaseAccountEndpoint\":\"https://contoso-westcentralus.documents.azure.com:443/\"}]}");
        assertThat(SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(account))
            .isEqualTo("contoso");
    }

    @Test(groups = "unit")
    public void canonicalAccountIdNormalizesRegionNameSpacesAndCase() {
        // Region "West US 3" normalises to "westus3" in the regional URL host.
        DatabaseAccount account = new DatabaseAccount(
            "{\"id\":\"contoso-westus3\","
                + "\"writableLocations\":[{\"name\":\"West US 3\","
                + "\"databaseAccountEndpoint\":\"https://contoso-westus3.documents.azure.com:443/\"}],"
                + "\"readableLocations\":[{\"name\":\"West US 3\","
                + "\"databaseAccountEndpoint\":\"https://contoso-westus3.documents.azure.com:443/\"}]}");
        assertThat(SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(account))
            .isEqualTo("contoso");
    }

    @Test(groups = "unit")
    public void canonicalAccountIdLeavesGlobalNameWithRegionShapedTailUnchanged() {
        // A global account legitimately named "my-acct-westus" must not be misinterpreted
        // as a regional id: rawId only matches the global endpoint, not any regional one.
        DatabaseAccount account = new DatabaseAccount(
            "{\"id\":\"my-acct-westus\","
                + "\"writableLocations\":[{\"name\":\"East US\","
                + "\"databaseAccountEndpoint\":\"https://my-acct-westus-eastus.documents.azure.com:443/\"}],"
                + "\"readableLocations\":[{\"name\":\"East US\","
                + "\"databaseAccountEndpoint\":\"https://my-acct-westus-eastus.documents.azure.com:443/\"}]}");
        assertThat(SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(account))
            .isEqualTo("my-acct-westus");
    }

    @Test(groups = "unit")
    public void canonicalAccountIdMatchesAcrossGlobalAndRegionalRepresentations() {
        // The defining property: global and any regional endpoint of the same account
        // canonicalise to the same id.
        String locationsJson =
            "\"writableLocations\":[{\"name\":\"East US\","
                + "\"databaseAccountEndpoint\":\"https://contoso-eastus.documents.azure.com:443/\"},"
                + "{\"name\":\"West Europe\","
                + "\"databaseAccountEndpoint\":\"https://contoso-westeurope.documents.azure.com:443/\"}],"
                + "\"readableLocations\":[{\"name\":\"East US\","
                + "\"databaseAccountEndpoint\":\"https://contoso-eastus.documents.azure.com:443/\"},"
                + "{\"name\":\"West Europe\","
                + "\"databaseAccountEndpoint\":\"https://contoso-westeurope.documents.azure.com:443/\"}]";

        DatabaseAccount fromGlobal = new DatabaseAccount("{\"id\":\"contoso\"," + locationsJson + "}");
        DatabaseAccount fromEastUs = new DatabaseAccount("{\"id\":\"contoso-eastus\"," + locationsJson + "}");
        DatabaseAccount fromWestEurope = new DatabaseAccount("{\"id\":\"contoso-westeurope\"," + locationsJson + "}");

        String canonical = SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(fromGlobal);
        assertThat(canonical).isEqualTo("contoso");
        assertThat(SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(fromEastUs)).isEqualTo(canonical);
        assertThat(SharedPartitionKeyRangeCacheRegistry.canonicalAccountId(fromWestEurope)).isEqualTo(canonical);
    }
}
