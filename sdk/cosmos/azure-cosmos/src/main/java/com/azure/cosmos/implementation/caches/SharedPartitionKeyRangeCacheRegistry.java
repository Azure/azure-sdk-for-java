// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.core.util.ReferenceManager;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process-wide registry of {@link AsyncCacheNonBlocking} instances holding
 * partition-key-range routing maps, keyed by service endpoint {@link URI}.
 * Multiple {@code CosmosAsyncClient} instances configured with the same
 * service endpoint share a single cache; the entry is refcounted and evicted
 * when the last caller releases.
 *
 * <p><b>Scope of sharing.</b> Two clients share only when their service
 * endpoints compare equal via {@link URI#equals(Object)} (which is
 * case-insensitive on host per RFC&nbsp;3986). Clients configured with
 * <em>different</em> endpoint URIs &mdash; including a regional endpoint
 * versus the global endpoint of the same account &mdash; do <b>not</b> share.
 * This avoids the brittle alternatives (e.g. canonicalising
 * {@link com.azure.cosmos.implementation.DatabaseAccount#getId() account ids}
 * returned from regional endpoints, which embed a service-side normalised
 * region suffix and cannot be reliably reversed).</p>
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

    private final ConcurrentHashMap<URI, Entry> entries = new ConcurrentHashMap<>();

    private SharedPartitionKeyRangeCacheRegistry() {
    }

    public static SharedPartitionKeyRangeCacheRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the shared cache for {@code endpoint} (creating it if needed) and
     * bumps the refcount. Returns an isolated cache when {@code endpoint} is
     * {@code null} or sharing is disabled.
     *
     * <p>If {@code owner} is non-null, a deferred cleanup action is registered
     * so the refcount is decremented automatically if {@code owner} is GC'd
     * without calling {@link #release(URI, AsyncCacheNonBlocking, ReleaseHandle)}.</p>
     */
    public AcquireResult acquire(URI endpoint, Object owner) {
        if (endpoint == null || !Configs.isSharedPartitionKeyRangeCacheEnabled()) {
            return new AcquireResult(new AsyncCacheNonBlocking<>(), null);
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

        ReleaseHandle handle = null;
        if (owner != null) {
            // The cleanup lambda MUST NOT capture `owner`, otherwise the owner can
            // never become phantom-reachable and the cleanup will never run.
            final URI capturedEndpoint = endpoint;
            final AsyncCacheNonBlocking<String, CollectionRoutingMap> capturedCache = entry.cache;
            final ReleaseHandle h = new ReleaseHandle();
            ReferenceManager.INSTANCE.register(owner, () -> {
                if (h.fulfill()) {
                    logger.warn(
                        "Leaked RxPartitionKeyRangeCache for endpoint [{}] released by"
                            + " ReferenceManager; always close the CosmosClient to avoid this.",
                        capturedEndpoint);
                    release(capturedEndpoint, capturedCache);
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
    public void release(URI endpoint,
                        AsyncCacheNonBlocking<String, CollectionRoutingMap> cache,
                        ReleaseHandle handle) {
        if (handle != null && !handle.fulfill()) {
            return;
        }
        release(endpoint, cache);
    }

    /** Refcount decrement; evicts the entry at zero. */
    public void release(URI endpoint, AsyncCacheNonBlocking<String, CollectionRoutingMap> cache) {
        if (endpoint == null || cache == null) {
            return;
        }

        entries.compute(endpoint, (key, existing) -> {
            if (existing == null || existing.cache != cache) {
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

    /** Test-only. */
    int registeredEndpointCount() {
        return entries.size();
    }

    /** Test-only. */
    int referenceCount(URI endpoint) {
        Entry entry = entries.get(endpoint);
        return entry == null ? 0 : entry.refCount.get();
    }

    /** Result of {@link #acquire(URI, Object)}: the cache plus a release handle (null when isolated). */
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
