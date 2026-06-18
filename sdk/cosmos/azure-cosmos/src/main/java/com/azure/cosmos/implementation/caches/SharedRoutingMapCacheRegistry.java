// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process-wide registry of {@link AsyncCacheNonBlocking} instances that hold
 * the cached partition-key-range routing maps, keyed by the Cosmos service
 * endpoint (account) the cache belongs to.
 *
 * <p>Historically, every {@link RxPartitionKeyRangeCache} owned its own
 * private {@link AsyncCacheNonBlocking}. When many {@code CosmosAsyncClient}
 * instances in the same JVM target the same account (a common multi-tenant /
 * multi-credential pattern), each client paid an independent memory cost for
 * the routing-map cache and independently issued {@code /pkranges} reads for
 * the same containers.
 *
 * <p>This registry lets clients targeting the same account share a single
 * cache instance. Sharing also strengthens the single-flight invariant
 * already provided by {@link AsyncCacheNonBlocking}: only one in-flight
 * {@code /pkranges} fetch per (account, container) at any time, even across
 * clients.</p>
 *
 * <p><b>Lifecycle.</b> Callers obtain a shared cache via {@link #acquire(String)}
 * during construction and return it via {@link #release(String, AsyncCacheNonBlocking)}
 * during {@code close()}. A per-entry refcount tracks live callers; when the
 * count reaches zero the entry is evicted so an idle endpoint does not pin
 * memory forever.</p>
 *
 * <p><b>Opt-out.</b> Setting the system property
 * {@code COSMOS.SHARED_PARTITION_KEY_RANGE_CACHE_ENABLED=false} disables
 * sharing; each {@link #acquire(String)} returns a fresh, isolated cache so
 * behaviour matches the pre-sharing implementation. The opt-out is read once
 * per {@link #acquire(String)} call so a test can toggle the property between
 * client constructions without restarting the JVM.</p>
 *
 * <p><b>Concurrency.</b> All state transitions go through
 * {@link ConcurrentHashMap#compute(Object, java.util.function.BiFunction)},
 * which gives atomic check-and-update under a per-key lock. No global lock
 * is required.</p>
 */
public final class SharedRoutingMapCacheRegistry {
    private static final Logger logger = LoggerFactory.getLogger(SharedRoutingMapCacheRegistry.class);

    private static final SharedRoutingMapCacheRegistry INSTANCE = new SharedRoutingMapCacheRegistry();

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    private SharedRoutingMapCacheRegistry() {
    }

    public static SharedRoutingMapCacheRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the shared {@link AsyncCacheNonBlocking} for the given endpoint,
     * creating it if necessary, and increments the refcount.
     *
     * <p>If {@code endpoint} is {@code null} or sharing is disabled via
     * {@link Configs#isSharedPartitionKeyRangeCacheEnabled()}, returns a fresh
     * isolated cache that the caller fully owns. {@link #release(String, AsyncCacheNonBlocking)}
     * is still safe to call on such a cache (it is a no-op).</p>
     *
     * @param endpoint The Cosmos service endpoint URL (e.g.
     *                 {@code "https://my-account.documents.azure.com:443/"}),
     *                 or {@code null} for an isolated cache.
     * @return The shared (or isolated) cache instance to use as routing-map storage.
     */
    public AsyncCacheNonBlocking<String, CollectionRoutingMap> acquire(String endpoint) {
        if (endpoint == null || !Configs.isSharedPartitionKeyRangeCacheEnabled()) {
            // Caller-owned cache; never enters the shared map.
            return new AsyncCacheNonBlocking<>();
        }

        Entry entry = entries.compute(endpoint, (key, existing) -> {
            if (existing == null) {
                Entry created = new Entry();
                created.refCount.set(1);
                logger.debug("Created shared partition key range cache for endpoint [{}]", key);
                return created;
            }
            existing.refCount.incrementAndGet();
            return existing;
        });
        return entry.cache;
    }

    /**
     * Releases a reference to the shared cache previously obtained via
     * {@link #acquire(String)}. When the last reference is released the
     * registry entry is evicted.
     *
     * <p>Safe to call when sharing was bypassed (null endpoint or sharing
     * disabled): the call is a no-op if the supplied cache is not the one
     * currently registered for {@code endpoint}. This makes the API safe to
     * call unconditionally from client close paths.</p>
     *
     * <p>Idempotency of the per-caller release contract is the caller's
     * responsibility (typically guarded by an {@code AtomicBoolean} in
     * {@link RxPartitionKeyRangeCache}). This method itself is safe to call
     * concurrently from multiple threads for distinct callers.</p>
     *
     * @param endpoint The endpoint the cache was acquired for, or {@code null}
     *                 if it was an isolated cache.
     * @param cache    The cache instance returned by {@link #acquire(String)}.
     */
    public void release(String endpoint, AsyncCacheNonBlocking<String, CollectionRoutingMap> cache) {
        if (endpoint == null || cache == null) {
            return;
        }

        entries.compute(endpoint, (key, existing) -> {
            if (existing == null || existing.cache != cache) {
                // Either sharing was disabled when this cache was acquired
                // (isolated cache, never registered) or another release()
                // already evicted the entry. Nothing to do.
                return existing;
            }
            int remaining = existing.refCount.decrementAndGet();
            if (remaining <= 0) {
                logger.debug("Evicting shared partition key range cache for endpoint [{}]", key);
                return null;
            }
            return existing;
        });
    }

    /**
     * Test-only: number of registered endpoints currently held by the registry.
     * Visible-for-testing.
     */
    int registeredEndpointCount() {
        return entries.size();
    }

    /**
     * Test-only: current refcount for an endpoint, or {@code 0} if no entry
     * is registered. Visible-for-testing.
     */
    int referenceCount(String endpoint) {
        Entry entry = entries.get(endpoint);
        return entry == null ? 0 : entry.refCount.get();
    }

    private static final class Entry {
        final AsyncCacheNonBlocking<String, CollectionRoutingMap> cache = new AsyncCacheNonBlocking<>();
        final AtomicInteger refCount = new AtomicInteger(0);
    }
}
