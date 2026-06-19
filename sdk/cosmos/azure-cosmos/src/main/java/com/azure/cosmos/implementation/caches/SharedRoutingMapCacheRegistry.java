// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
 * two clients to the same account that bootstrap from <i>different</i> regional
 * endpoints (e.g. {@code my-acct-westus.documents.azure.com} vs
 * {@code my-acct.documents.azure.com}) do <i>not</i> share a cache entry.</p>
 *
 * <p><b>Lifecycle.</b> Callers obtain a shared cache via {@link #acquire(URI, Object)}
 * during construction and return it via {@link #release(URI, AsyncCacheNonBlocking)}
 * during {@code close()}. A per-entry refcount tracks live callers; when the
 * count reaches zero the entry is evicted so an idle endpoint does not pin
 * memory forever.</p>
 *
 * <p><b>Leaked-client safety net.</b> A caller may forget to {@code close()}
 * its {@code CosmosAsyncClient}. Without protection the unclosed client would
 * keep a strong reference to the shared cache and pin it for the JVM's
 * lifetime. To handle that, every {@link #acquire(URI, Object)} also
 * registers a {@link PhantomReference} for the owner. When the owner becomes
 * unreachable the GC enqueues the phantom; a single daemon reaper thread
 * drains the queue and calls {@link #release(URI, AsyncCacheNonBlocking)}
 * to clean up. The reaper is not a substitute for
 * {@link java.io.Closeable#close()} (no guaranteed promptness) but it
 * prevents the cache from leaking forever. This mirrors the safety net
 * Python provides via its {@code __del__} fallback and Rust gets for free
 * via {@code Drop}; we cannot use {@code java.lang.ref.Cleaner} because
 * this SDK still supports Java 8.</p>
 *
 * <p><b>Opt-out.</b> Setting the system property
 * {@code COSMOS.SHARED_PARTITION_KEY_RANGE_CACHE_ENABLED=false} disables
 * sharing; each {@link #acquire(URI, Object)} returns a fresh, isolated cache
 * (no registry entry, no phantom registration).</p>
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

    /**
     * Queue the GC enqueues {@link OwnerPhantom} instances onto when their
     * referent (the owning {@link RxPartitionKeyRangeCache}) becomes
     * unreachable without {@code close()} having been called.
     */
    private final ReferenceQueue<Object> reaperQueue = new ReferenceQueue<>();

    /**
     * Strong-references every live {@link OwnerPhantom} so the JVM does not
     * collect the phantom before its referent. The GC only enqueues phantoms
     * that are themselves still reachable; without this set the phantoms
     * registered in {@link #acquire(URI, Object)} would be garbage-collected
     * together with their owners and the reaper would never observe the leak.
     * Entries are removed either by the reaper after processing or by
     * {@link #release(URI, AsyncCacheNonBlocking, PhantomReference)} on prompt close.
     */
    private final Set<OwnerPhantom> livePhantoms =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    private SharedRoutingMapCacheRegistry() {
        Thread reaper = new Thread(this::runReaper, "cosmos-shared-pkr-cache-reaper");
        reaper.setDaemon(true);
        reaper.start();
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
     * isolated cache that the caller fully owns.
     * {@link #release(URI, AsyncCacheNonBlocking)} is still safe to call on
     * such a cache (it is a no-op).</p>
     *
     * <p>When {@code owner} is non-null and sharing is enabled, a
     * {@link PhantomReference} to {@code owner} is registered so that an
     * unreferenced (leaked) owner triggers a deferred release.</p>
     *
     * @param endpoint The Cosmos service endpoint URI, or {@code null} for an isolated cache.
     * @param owner    The object whose unreachability should trigger a deferred release
     *                 (typically the {@link RxPartitionKeyRangeCache} caller). May be {@code null}
     *                 to skip phantom registration (e.g. tests calling acquire directly).
     * @return A handle that exposes the shared cache instance plus a token used by
     *         {@link #release(URI, AsyncCacheNonBlocking)} for prompt cleanup.
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

        OwnerPhantom phantom = null;
        if (owner != null) {
            phantom = new OwnerPhantom(owner, reaperQueue, endpoint, entry.cache);
            // The phantom MUST be strongly reachable until it is either processed by
            // the reaper or cleared on prompt close, otherwise the GC will collect it
            // alongside the owner without ever enqueueing it.
            livePhantoms.add(phantom);
        }
        return new AcquireResult(entry.cache, phantom);
    }

    /**
     * Convenience overload used by tests that do not need phantom registration.
     */
    AsyncCacheNonBlocking<String, CollectionRoutingMap> acquire(URI endpoint) {
        return acquire(endpoint, null).cache;
    }

    /**
     * Releases a reference to the shared cache previously obtained via
     * {@link #acquire(URI, Object)}. When the last reference is released the
     * registry entry is evicted.
     *
     * <p>Safe to call when sharing was bypassed (null endpoint or sharing
     * disabled): the call is a no-op if the supplied cache is not the one
     * currently registered for {@code endpoint}.</p>
     *
     * <p>If {@code phantom} is non-null it is cleared and dropped from the
     * registry's live-phantom set, so the reaper does not later double-release
     * the same reference.</p>
     *
     * @param endpoint The endpoint the cache was acquired for, or {@code null}
     *                 if it was an isolated cache.
     * @param cache    The cache instance returned by {@link #acquire(URI, Object)}.
     * @param phantom  The phantom returned by {@link #acquire(URI, Object)}, or
     *                 {@code null} if none was registered.
     */
    public void release(URI endpoint,
                        AsyncCacheNonBlocking<String, CollectionRoutingMap> cache,
                        PhantomReference<?> phantom) {
        if (phantom instanceof OwnerPhantom) {
            OwnerPhantom op = (OwnerPhantom) phantom;
            livePhantoms.remove(op);
            op.clear();
        }
        release(endpoint, cache);
    }

    /**
     * Internal release used by the reaper (which already holds the phantom
     * reference it processed) and by the two-arg overload above. Test code
     * also calls this overload directly to simulate close paths.
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
     * Drains the reference queue and releases the shared cache reference for
     * each phantom whose owner was garbage-collected without a prior
     * {@code close()}. Runs forever on a daemon thread; exits cleanly on
     * interruption (e.g. JVM shutdown).
     */
    private void runReaper() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Reference<?> ref = reaperQueue.remove();
                if (ref instanceof OwnerPhantom) {
                    OwnerPhantom phantom = (OwnerPhantom) ref;
                    logger.warn(
                        "Leaked (unclosed) RxPartitionKeyRangeCache detected for endpoint [{}]"
                            + " — releasing shared cache reference via reaper. Always close CosmosClient"
                            + " / CosmosAsyncClient to avoid relying on this safety net.",
                        phantom.endpoint);
                    try {
                        release(phantom.endpoint, phantom.cache);
                    } catch (RuntimeException ex) {
                        logger.error("Reaper failed to release shared cache for endpoint [{}]",
                            phantom.endpoint, ex);
                    } finally {
                        livePhantoms.remove(phantom);
                        phantom.clear();
                    }
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
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
     * Test-only: number of live phantoms currently retained by the registry.
     */
    int livePhantomCount() {
        return livePhantoms.size();
    }

    /**
     * Returned by {@link #acquire(URI, Object)}. Holds the shared cache and
     * the {@link PhantomReference} (when one was registered) so the caller
     * can clear it on prompt {@code close()} and avoid a redundant reaper
     * release later.
     */
    public static final class AcquireResult {
        public final AsyncCacheNonBlocking<String, CollectionRoutingMap> cache;
        public final PhantomReference<?> ownerPhantom;

        AcquireResult(AsyncCacheNonBlocking<String, CollectionRoutingMap> cache,
                      PhantomReference<?> ownerPhantom) {
            this.cache = cache;
            this.ownerPhantom = ownerPhantom;
        }
    }

    private static final class Entry {
        final AsyncCacheNonBlocking<String, CollectionRoutingMap> cache = new AsyncCacheNonBlocking<>();
        final AtomicInteger refCount = new AtomicInteger(0);
    }

    /**
     * PhantomReference whose enqueueing on GC of its referent (the owning
     * {@link RxPartitionKeyRangeCache}) signals the reaper to release the
     * matching shared-cache entry. The captured endpoint + cache are strong
     * fields on the phantom itself — that's fine: once the reaper clears the
     * phantom, the phantom itself becomes unreachable and the captured cache
     * loses one more strong holder.
     *
     * <p><b>Why is this safe?</b> The phantom is registered on the
     * {@code RxPartitionKeyRangeCache} (which is the field of the
     * {@code RxDocumentClientImpl}). It does <i>not</i> reference the client
     * or any field of it. So the phantom does not prevent the owning client
     * from being GC'd; it is enqueued exactly when the owning cache
     * becomes phantom-reachable.</p>
     *
     * <p>The phantom is kept reachable by being assigned to a field on the
     * owning {@code RxPartitionKeyRangeCache}. If the phantom itself were
     * unreachable before its referent became unreachable, the GC would never
     * enqueue it. Holding it on the owner ties phantom liveness to owner
     * liveness, which is precisely what we want.</p>
     */
    static final class OwnerPhantom extends PhantomReference<Object> {
        final URI endpoint;
        final AsyncCacheNonBlocking<String, CollectionRoutingMap> cache;

        OwnerPhantom(Object owner,
                     ReferenceQueue<Object> queue,
                     URI endpoint,
                     AsyncCacheNonBlocking<String, CollectionRoutingMap> cache) {
            super(owner, queue);
            this.endpoint = endpoint;
            this.cache = cache;
        }
    }
}
