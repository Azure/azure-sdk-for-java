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
 * Process-wide registry of {@link AsyncCacheNonBlocking} instances that hold
 * the cached partition-key-range routing maps, keyed by the Cosmos service
 * endpoint URI.
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
 * <p><b>Key identity.</b> The {@link URI} is used directly so
 * {@link URI#equals(Object)}'s case-insensitive host comparison applies — two
 * clients built with {@code https://Acct.documents.azure.com/} and
 * {@code https://acct.documents.azure.com/} share a single cache entry as
 * intended.</p>
 *
 * <p><b>Cross-SDK consistency.</b> The peer Cosmos DB SDKs key sharing on the
 * user-supplied account endpoint URL: Python uses {@code client.url_connection}
 * (raw string compare); Rust uses {@code AccountEndpoint(Url)} (URL-based
 * equality). This implementation matches that contract. As a consequence,
 * two clients to the same account that bootstrap from <i>different</i>
 * regional endpoints (e.g. {@code my-acct-westus.documents.azure.com} vs
 * {@code my-acct.documents.azure.com}) do <i>not</i> share a cache entry —
 * the same fragmentation behaviour the peer SDKs have.</p>
 *
 * <p><b>Lifecycle.</b> Callers obtain a shared cache via
 * {@link #acquire(URI, Object)} during construction and return it via
 * {@link #release(URI, AsyncCacheNonBlocking, ReleaseHandle)} during
 * {@code close()}. A per-entry refcount tracks live callers; when the count
 * reaches zero the entry is evicted so an idle endpoint does not pin memory
 * forever.</p>
 *
 * <p><b>Leaked-client safety net.</b> A caller may forget to {@code close()}
 * its {@code CosmosAsyncClient}. Without protection the unclosed client would
 * keep a strong reference to the shared cache and pin it for the JVM's
 * lifetime. To handle that, every {@link #acquire(URI, Object)} also
 * registers a cleanup action with {@link ReferenceManager#INSTANCE} (the
 * SDK-wide reference manager in {@code azure-core}). When the owner object
 * becomes phantom-reachable, the reference manager runs the cleanup action
 * which decrements the refcount and evicts the entry if it was the last
 * reference. On Java 9+ {@code azure-core}'s {@code ReferenceManagerImpl}
 * delegates to {@link java.lang.ref.Cleaner} reflectively; on Java 8 it uses
 * an internal {@link java.lang.ref.PhantomReference}-based daemon thread.
 * Cosmos reuses the supported, well-tested azure-core machinery rather than
 * rolling its own.</p>
 *
 * <p><b>Opt-out.</b> Setting the system property
 * {@code COSMOS.SHARED_PARTITION_KEY_RANGE_CACHE_ENABLED=false} disables
 * sharing; each {@link #acquire(URI, Object)} returns a fresh, isolated cache
 * (no registry entry, no cleanup registration).</p>
 *
 * <p><b>Concurrency.</b> All state transitions go through
 * {@link ConcurrentHashMap#compute(Object, java.util.function.BiFunction)},
 * which gives atomic check-and-update under a per-key lock. No global lock
 * is required.</p>
 */
public final class SharedRoutingMapCacheRegistry {
    private static final Logger logger = LoggerFactory.getLogger(SharedRoutingMapCacheRegistry.class);

    private static final SharedRoutingMapCacheRegistry INSTANCE = new SharedRoutingMapCacheRegistry();

    private final ConcurrentHashMap<URI, Entry> entries = new ConcurrentHashMap<>();

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
     * isolated cache that the caller fully owns.</p>
     *
     * <p>When {@code owner} is non-null and sharing is enabled, a cleanup
     * action is registered with {@link ReferenceManager#INSTANCE} so that an
     * unreferenced (leaked) owner triggers a deferred release.</p>
     *
     * @param endpoint The Cosmos service endpoint URI, or {@code null} for an isolated cache.
     * @param owner    The object whose unreachability should trigger a deferred release
     *                 (typically the {@link RxPartitionKeyRangeCache} caller). May be {@code null}
     *                 to skip cleanup registration (e.g. tests calling acquire directly).
     * @return A handle that exposes the shared cache instance plus a token used by
     *         {@link RxPartitionKeyRangeCache#close()} to mark the cleanup action
     *         already-fulfilled so it becomes a no-op when {@link ReferenceManager}
     *         later runs it.
     */
    public AcquireResult acquire(URI endpoint, Object owner) {
        if (endpoint == null || !Configs.isSharedPartitionKeyRangeCacheEnabled()) {
            // Caller-owned cache; never enters the shared map.
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
            // IMPORTANT: the cleanup action must NOT capture `owner`, or the
            // owner will never become phantom-reachable. We capture only the
            // endpoint URI and the cache reference — both independent of the owner.
            final URI capturedEndpoint = endpoint;
            final AsyncCacheNonBlocking<String, CollectionRoutingMap> capturedCache = entry.cache;
            final ReleaseHandle h = new ReleaseHandle();
            ReferenceManager.INSTANCE.register(owner, () -> {
                if (h.fulfill()) {
                    logger.warn(
                        "Leaked (unclosed) RxPartitionKeyRangeCache detected for endpoint [{}]"
                            + " — releasing shared cache reference via ReferenceManager. Always"
                            + " close CosmosClient / CosmosAsyncClient to avoid relying on this"
                            + " safety net.",
                        capturedEndpoint);
                    release(capturedEndpoint, capturedCache);
                }
            });
            handle = h;
        }
        return new AcquireResult(entry.cache, handle);
    }

    /**
     * Convenience overload used by tests that do not need cleanup registration.
     */
    AsyncCacheNonBlocking<String, CollectionRoutingMap> acquire(URI endpoint) {
        return acquire(endpoint, null).cache;
    }

    /**
     * Prompt-close path used by {@link RxPartitionKeyRangeCache#close()}.
     * Marks the cleanup action as fulfilled (so the later
     * {@link ReferenceManager}-triggered run becomes a no-op) and decrements
     * the refcount.
     */
    public void release(URI endpoint,
                        AsyncCacheNonBlocking<String, CollectionRoutingMap> cache,
                        ReleaseHandle handle) {
        if (handle != null && !handle.fulfill()) {
            // Already fulfilled by the ReferenceManager path; do not double-decrement.
            return;
        }
        release(endpoint, cache);
    }

    /**
     * Internal release used by both the prompt-close path and the
     * ReferenceManager-triggered cleanup action.
     */
    public void release(URI endpoint, AsyncCacheNonBlocking<String, CollectionRoutingMap> cache) {
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
     */
    int registeredEndpointCount() {
        return entries.size();
    }

    /**
     * Test-only: current refcount for an endpoint, or {@code 0} if no entry
     * is registered.
     */
    int referenceCount(URI endpoint) {
        Entry entry = entries.get(endpoint);
        return entry == null ? 0 : entry.refCount.get();
    }

    /**
     * Returned by {@link #acquire(URI, Object)}. Holds the shared cache and
     * a handle the caller passes back to
     * {@link #release(URI, AsyncCacheNonBlocking, ReleaseHandle)} on prompt
     * close to prevent the deferred cleanup action from double-releasing.
     */
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
     * One-shot fulfilment flag shared between the prompt-close path and the
     * deferred {@link ReferenceManager} cleanup. Whichever path runs first
     * wins via {@link AtomicBoolean#compareAndSet}; the loser becomes a no-op
     * so the refcount is decremented exactly once.
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
