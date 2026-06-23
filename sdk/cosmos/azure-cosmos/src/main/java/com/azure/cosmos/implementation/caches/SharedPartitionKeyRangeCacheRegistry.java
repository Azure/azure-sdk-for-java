// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.core.util.ReferenceManager;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.DatabaseAccount;
import com.azure.cosmos.implementation.DatabaseAccountLocation;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.RegionNameNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process-wide registry of {@link AsyncCacheNonBlocking} instances holding
 * partition-key-range routing maps, keyed by Cosmos database account id.
 * Multiple {@code CosmosAsyncClient} instances targeting the same account
 * share a single cache; the entry is refcounted and evicted when the last
 * caller releases.
 *
 * <p>An unreleased caller is cleaned up by registering a one-shot action with
 * {@link ReferenceManager#INSTANCE}; when the owner becomes phantom-reachable
 * the action decrements the refcount.</p>
 *
 * <p>Sharing can be disabled via system property
 * {@code COSMOS.SHARED_PARTITION_KEY_RANGE_CACHE_ENABLED=false}.</p>
 */
public final class SharedPartitionKeyRangeCacheRegistry {
    private static final Logger logger = LoggerFactory.getLogger(SharedPartitionKeyRangeCacheRegistry.class);

    private static final SharedPartitionKeyRangeCacheRegistry INSTANCE = new SharedPartitionKeyRangeCacheRegistry();

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    private SharedPartitionKeyRangeCacheRegistry() {
    }

    public static SharedPartitionKeyRangeCacheRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the shared cache for {@code accountId} (creating it if needed) and
     * bumps the refcount. Returns an isolated cache when {@code accountId} is
     * {@code null}/blank or sharing is disabled.
     *
     * <p>If {@code owner} is non-null, a deferred cleanup action is registered
     * so the refcount is decremented automatically if {@code owner} is GC'd
     * without calling {@link #release(String, AsyncCacheNonBlocking, ReleaseHandle)}.</p>
     */
    public AcquireResult acquire(String accountId, Object owner) {
        if (accountId == null || accountId.isEmpty() || !Configs.isSharedPartitionKeyRangeCacheEnabled()) {
            return new AcquireResult(new AsyncCacheNonBlocking<>(), null);
        }

        Entry entry = entries.compute(accountId, (key, existing) -> {
            if (existing == null) {
                Entry created = new Entry();
                created.refCount.set(1);
                logger.debug("Created shared partition key range cache for account [{}]", key);
                return created;
            }
            existing.refCount.incrementAndGet();
            return existing;
        });

        ReleaseHandle handle = null;
        if (owner != null) {
            // The cleanup lambda MUST NOT capture `owner`, otherwise the owner can
            // never become phantom-reachable and the cleanup will never run.
            final String capturedAccountId = accountId;
            final AsyncCacheNonBlocking<String, CollectionRoutingMap> capturedCache = entry.cache;
            final ReleaseHandle h = new ReleaseHandle();
            ReferenceManager.INSTANCE.register(owner, () -> {
                if (h.fulfill()) {
                    logger.warn(
                        "Leaked RxPartitionKeyRangeCache for account [{}] released by"
                            + " ReferenceManager; always close the CosmosClient to avoid this.",
                        capturedAccountId);
                    release(capturedAccountId, capturedCache);
                }
            });
            handle = h;
        }
        return new AcquireResult(entry.cache, handle);
    }

    /**
     * Prompt release path. Fulfils {@code handle} (so the deferred cleanup
     * becomes a no-op) and decrements the refcount. If the handle was already
     * fulfilled by the deferred cleanup, this call is a no-op.
     */
    public void release(String accountId,
                        AsyncCacheNonBlocking<String, CollectionRoutingMap> cache,
                        ReleaseHandle handle) {
        if (handle != null && !handle.fulfill()) {
            return;
        }
        release(accountId, cache);
    }

    /** Refcount decrement; evicts the entry at zero. */
    public void release(String accountId, AsyncCacheNonBlocking<String, CollectionRoutingMap> cache) {
        if (accountId == null || accountId.isEmpty() || cache == null) {
            return;
        }

        entries.compute(accountId, (key, existing) -> {
            if (existing == null || existing.cache != cache) {
                return existing;
            }
            int remaining = existing.refCount.decrementAndGet();
            if (remaining <= 0) {
                logger.debug("Evicting shared partition key range cache for account [{}]", key);
                return null;
            }
            return existing;
        });
    }

    /** Test-only. */
    int registeredAccountCount() {
        return entries.size();
    }

    /** Test-only. */
    int referenceCount(String accountId) {
        Entry entry = entries.get(accountId);
        return entry == null ? 0 : entry.refCount.get();
    }

    /**
     * Canonicalizes the account id returned by {@link DatabaseAccount#getId()} so that
     * clients constructed against a regional endpoint share the registry entry with
     * clients constructed against the global endpoint of the same logical account.
     *
     * <p>The Cosmos service returns {@code getId() == "<globalAccountId>-<normalize(region)>"}
     * from regional endpoints (matching the regional URL host segment built by
     * {@link com.azure.cosmos.implementation.routing.LocationHelper#getLocationEndpoint}),
     * while the global endpoint returns just {@code "<globalAccountId>"}. To map both to
     * the same key, this method:</p>
     *
     * <ol>
     *   <li>Walks {@link DatabaseAccount#getReadableLocations()} and
     *       {@link DatabaseAccount#getWritableLocations()}.</li>
     *   <li>Extracts each location's regional account-id (the host prefix of
     *       {@link DatabaseAccountLocation#getEndpoint()}, i.e. the part before the first
     *       {@code .}).</li>
     *   <li>When the raw id equals any such regional account-id, strips the trailing
     *       {@code "-" + RegionNameNormalizer.normalize(loc.getName())} suffix.</li>
     * </ol>
     *
     * <p>The two-step match (regional-host equality <em>plus</em> normalized-region suffix)
     * is robust against legitimate global account names that happen to end in a hyphenated
     * region-shaped tail: stripping only occurs when the id provably came from one of the
     * regional endpoints reported by the service.</p>
     *
     * <p>Returns the raw id unchanged when the input is null/empty, when no regional
     * location matches, or when the account has no locations metadata.</p>
     */
    public static String canonicalAccountId(DatabaseAccount account) {
        if (account == null) {
            return null;
        }
        String rawId = account.getId();
        if (StringUtils.isEmpty(rawId)) {
            return rawId;
        }
        for (Iterable<DatabaseAccountLocation> locations : Arrays.asList(
                account.getReadableLocations(), account.getWritableLocations())) {
            if (locations == null) {
                continue;
            }
            for (DatabaseAccountLocation loc : locations) {
                if (loc == null) {
                    continue;
                }
                String locEndpoint = loc.getEndpoint();
                String locName = loc.getName();
                if (StringUtils.isEmpty(locEndpoint) || StringUtils.isEmpty(locName)) {
                    continue;
                }
                String regionalAccountId = extractAccountIdFromEndpoint(locEndpoint);
                if (regionalAccountId == null || !regionalAccountId.equals(rawId)) {
                    continue;
                }
                String suffix = "-" + RegionNameNormalizer.normalize(locName);
                if (rawId.length() > suffix.length() && rawId.endsWith(suffix)) {
                    return rawId.substring(0, rawId.length() - suffix.length());
                }
            }
        }
        return rawId;
    }

    private static String extractAccountIdFromEndpoint(String endpoint) {
        try {
            String host = URI.create(endpoint).getHost();
            if (host == null) {
                return null;
            }
            int dot = host.indexOf('.');
            return dot >= 0 ? host.substring(0, dot) : host;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Result of {@link #acquire(String, Object)}: the cache plus a release handle (null when isolated). */
    public static final class AcquireResult {
        public final AsyncCacheNonBlocking<String, CollectionRoutingMap> cache;
        public final ReleaseHandle releaseHandle;

        AcquireResult(AsyncCacheNonBlocking<String, CollectionRoutingMap> cache,
                      ReleaseHandle releaseHandle) {
            this.cache = cache;
            this.releaseHandle = releaseHandle;
        }
    }

    /**
     * One-shot CAS flag shared between prompt-close and deferred cleanup;
     * guarantees exactly one refcount decrement.
     */
    public static final class ReleaseHandle {
        private final AtomicBoolean fulfilled = new AtomicBoolean(false);

        boolean fulfill() {
            return fulfilled.compareAndSet(false, true);
        }
    }

    private static final class Entry {
        final AsyncCacheNonBlocking<String, CollectionRoutingMap> cache = new AsyncCacheNonBlocking<>();
        final AtomicInteger refCount = new AtomicInteger(0);
    }
}
