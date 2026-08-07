// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.utils;

import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * A bounded, access-ordered cache for AIA resolution results.
 *
 * <p>Completed entries and refresh suppressions use short synchronized sections. Loaders run outside that lock.
 * Concurrent loads or refreshes for the same URL share one in-flight result without blocking other URLs.
 */
final class AiaResponseCache {
    private final int maximumSize;
    private final LongSupplier clock;
    private final Map<String, Entry> entries = new LinkedHashMap<>(16, 0.75f, true);
    private final Map<SuppressionKey, Suppression> refreshSuppressions = new LinkedHashMap<>(16, 0.75f, true);
    private final ConcurrentHashMap<String, CompletableFuture<Entry>> inFlight = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<Entry>> refreshes = new ConcurrentHashMap<>();
    // Monotonic entry version; intentionally not reset by clear() so observed generations cannot collide.
    private final AtomicLong generations = new AtomicLong();
    // Cache-wide invalidation version that prevents requests started before clear() from repopulating the cache.
    private long epoch;

    /**
     * Creates a cache with a maximum number of completed entries.
     *
     * @param maximumSize the maximum number of completed entries and refresh suppressions
     * @param clock the clock used to evaluate expiration times
     */
    AiaResponseCache(int maximumSize, LongSupplier clock) {
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("maximumSize must be greater than zero");
        }
        this.maximumSize = maximumSize;
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    /**
     * Returns a fresh cached response, or loads and caches one when absent.
     *
     * @param url the AIA URL used as the cache key
     * @param loader the response loader used on a cache miss
     * @return the certificates in the cached or loaded response
     */
    List<X509Certificate> getOrLoad(String url, Loader loader) {
        return getOrLoadResult(url, loader, () -> {
        }).certificates;
    }

    /**
     * Returns a fresh cached response and runs an action when the response comes from the cache.
     *
     * @param url the AIA URL used as the cache key
     * @param loader the response loader used on a cache miss
     * @param cacheHitAction the action to run on a cache hit
     * @return the certificates in the cached or loaded response
     */
    List<X509Certificate> getOrLoad(String url, Loader loader, Runnable cacheHitAction) {
        return getOrLoadResult(url, loader, cacheHitAction).certificates;
    }

    /**
     * Returns a response together with its generation and source.
     *
     * <p>Concurrent misses for the same URL share one loader call.
     *
     * @param url the AIA URL used as the cache key
     * @param loader the response loader used on a cache miss
     * @param cacheHitAction the action to run on a cache hit
     * @return the cached or loaded response and its cache metadata
     */
    LookupResult getOrLoadResult(String url, Loader loader, Runnable cacheHitAction) {
        Objects.requireNonNull(url, "url cannot be null");
        Objects.requireNonNull(loader, "loader cannot be null");
        Objects.requireNonNull(cacheHitAction, "cacheHitAction cannot be null");
        Entry cached = getIfFresh(url);
        if (cached != null) {
            cacheHitAction.run();
            return new LookupResult(cached, Source.CACHE);
        }

        CompletableFuture<Entry> created = new CompletableFuture<>();
        CompletableFuture<Entry> existing = inFlight.putIfAbsent(url, created);
        if (existing != null) {
            return new LookupResult(await(existing), Source.LOAD);
        }

        long loadEpoch = getEpoch(); // Captured before loading so clear() can invalidate the pending publication.
        try {
            Entry rechecked = getIfFresh(url);
            Entry result = rechecked != null ? rechecked : Objects.requireNonNull(loader.load(), "loader result");
            if (rechecked != null) {
                cacheHitAction.run();
                created.complete(rechecked);
                return new LookupResult(rechecked, Source.CACHE);
            }
            Entry published = putIfFresh(url, result, loadEpoch);
            created.complete(published);
            return new LookupResult(published, Source.LOAD);
        } catch (RuntimeException e) {
            created.completeExceptionally(e);
            return new LookupResult(created.join(), Source.LOAD);
        } finally {
            if (!created.isDone()) {
                created.completeExceptionally(
                    new IllegalStateException("AIA resolution terminated before producing a result"));
            }
            inFlight.remove(url, created);
        }
    }

    /**
     * Refreshes an entry only if it has not changed since the caller observed it.
     *
     * <p>Concurrent refreshes for the same URL share one loader call. A failed refresh does not replace an existing
     * positive entry.
     *
     * @param url the AIA URL used as the cache key
     * @param observedGeneration the generation observed by the caller
     * @param loader the response loader used when a refresh is still required
     * @return the current or refreshed response and its cache metadata
     */
    LookupResult refreshIfUnchanged(String url, long observedGeneration, Loader loader) {
        Objects.requireNonNull(url, "url cannot be null");
        Objects.requireNonNull(loader, "loader cannot be null");
        Entry current = getIfFresh(url);
        // A different generation means another caller has already published a newer response for this URL.
        if (current != null && current.generation != observedGeneration) {
            return new LookupResult(current, Source.CACHE);
        }

        CompletableFuture<Entry> created = new CompletableFuture<>();
        CompletableFuture<Entry> existing = refreshes.putIfAbsent(url, created);
        if (existing != null) {
            return new LookupResult(await(existing), Source.REFRESH);
        }

        long refreshEpoch = getEpoch(); // Applies the same clear() barrier to forced refreshes.
        try {
            Entry rechecked = getIfFresh(url);
            // Close the race between the first generation check and winning the refresh single-flight registration.
            if (rechecked != null && rechecked.generation != observedGeneration) {
                created.complete(rechecked);
                return new LookupResult(rechecked, Source.CACHE);
            }

            Entry loaded = Objects.requireNonNull(loader.load(), "loader result");
            Entry result = shouldReplaceOnRefresh(loaded)
                ? putIfFresh(url, loaded, refreshEpoch)
                : loaded.withGeneration(rechecked == null ? observedGeneration : rechecked.generation);
            created.complete(result);
            return new LookupResult(result, Source.REFRESH);
        } catch (RuntimeException e) {
            created.completeExceptionally(e);
            return new LookupResult(created.join(), Source.REFRESH);
        } finally {
            if (!created.isDone()) {
                created.completeExceptionally(
                    new IllegalStateException("AIA refresh terminated before producing a result"));
            }
            refreshes.remove(url, created);
        }
    }

    /** Clears cached and coordination state, advancing the epoch so pending requests cannot repopulate the cache. */
    synchronized void clear() {
        epoch++;
        entries.clear();
        refreshSuppressions.clear();
        inFlight.clear();
        refreshes.clear();
    }

    /**
     * Returns the number of fresh completed entries.
     *
     * @return the number of fresh completed entries
     */
    synchronized int size() {
        removeExpiredEntries(clock.getAsLong());
        return entries.size();
    }

    /**
     * Checks whether a recent miss suppresses another refresh for the same target and entry generation.
     *
     * @param url the AIA URL
     * @param targetIdentity the identity of the certificate that needs an issuer
     * @param generation the current URL entry generation
     * @return true when another refresh should be suppressed
     */
    synchronized boolean isRefreshSuppressed(String url, Object targetIdentity, long generation) {
        SuppressionKey key = new SuppressionKey(url, targetIdentity);
        Suppression suppression = refreshSuppressions.get(key);
        if (suppression == null) {
            return false;
        }
        if (suppression.isExpired(clock.getAsLong()) || suppression.generation != generation) {
            refreshSuppressions.remove(key);
            return false;
        }
        return true;
    }

    /**
     * Suppresses another refresh for one target and URL entry generation.
     *
     * @param url the AIA URL
     * @param targetIdentity the identity of the certificate that needs an issuer
     * @param generation the current URL entry generation
     * @param ttlInMillis the suppression duration in milliseconds
     */
    synchronized void suppressRefresh(String url, Object targetIdentity, long generation, long ttlInMillis) {
        long now = clock.getAsLong();
        removeExpiredSuppressions(now);
        refreshSuppressions.put(new SuppressionKey(url, targetIdentity),
            new Suppression(generation, safeAdd(now, ttlInMillis)));
        while (refreshSuppressions.size() > maximumSize) {
            Iterator<SuppressionKey> iterator = refreshSuppressions.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
    }

    /**
     * Removes a refresh suppression after the target issuer is resolved.
     *
     * @param url the AIA URL
     * @param targetIdentity the identity of the certificate that needs an issuer
     */
    synchronized void clearRefreshSuppression(String url, Object targetIdentity) {
        refreshSuppressions.remove(new SuppressionKey(url, targetIdentity));
    }

    private synchronized Entry getIfFresh(String url) {
        Entry entry = entries.get(url);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired(clock.getAsLong())) {
            entries.remove(url);
            return null;
        }
        return entry;
    }

    /**
     * Publishes a fresh entry only when no clear occurred after its request started, assigning a new generation.
     *
     * @param url the AIA URL used as the cache key
     * @param entry the loaded entry
     * @param expectedEpoch the epoch captured before loading started
     * @return the published entry, or the original unpublished entry when publication is rejected
     */
    private synchronized Entry putIfFresh(String url, Entry entry, long expectedEpoch) {
        long now = clock.getAsLong();
        removeExpiredEntries(now);
        if (entry.isExpired(now) || epoch != expectedEpoch) {
            return entry;
        }

        Entry versioned = entry.withGeneration(generations.incrementAndGet());
        entries.put(url, versioned);
        while (entries.size() > maximumSize) {
            Iterator<String> iterator = entries.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
        return versioned;
    }

    private synchronized long getEpoch() {
        return epoch;
    }

    /**
     * Checks whether a refresh result can replace the current positive entry.
     *
     * @param entry the refresh result
     * @return true when the result is positive and fresh
     */
    private boolean shouldReplaceOnRefresh(Entry entry) {
        return !entry.certificates.isEmpty() && !entry.isExpired(clock.getAsLong());
    }

    private void removeExpiredEntries(long now) {
        entries.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private void removeExpiredSuppressions(long now) {
        refreshSuppressions.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private static long safeAdd(long value, long increment) {
        return increment > Long.MAX_VALUE - value ? Long.MAX_VALUE : value + increment;
    }

    private static Entry await(CompletableFuture<Entry> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return propagate(e);
        } catch (ExecutionException e) {
            return propagate(e.getCause());
        }
    }

    private static Entry propagate(Throwable cause) {
        CompletableFuture<Entry> failed = new CompletableFuture<>();
        failed.completeExceptionally(cause);
        return failed.join();
    }

    interface Loader {
        /**
         * Loads and parses one AIA response.
         *
         * @return the loaded cache entry
         */
        Entry load();
    }

    /** Identifies how a lookup result was obtained. */
    enum Source {
        /** A fresh completed entry. */
        CACHE,

        /** A normal cache-miss load. */
        LOAD,

        /** A forced refresh of an observed entry. */
        REFRESH
    }

    /**
     * A cache response with the metadata needed to decide whether a forced refresh is safe.
     */
    static final class LookupResult {
        private final List<X509Certificate> certificates;
        private final long generation;
        private final Source source;
        private final boolean negative;

        private LookupResult(Entry entry, Source source) {
            this.certificates = entry.certificates;
            this.generation = entry.generation;
            this.source = source;
            this.negative = entry.certificates.isEmpty();
        }

        /**
         * Gets the certificates in the response.
         *
         * @return the response certificates
         */
        List<X509Certificate> getCertificates() {
            return certificates;
        }

        /**
         * Gets the generation assigned when the URL entry was cached.
         *
         * @return the entry generation, or zero for a response that was not published
         */
        long getGeneration() {
            return generation;
        }

        /**
         * Gets the source of the response.
         *
         * @return the response source
         */
        Source getSource() {
            return source;
        }

        /**
         * Indicates whether the response contains no certificates.
         *
         * @return true when the response is negative
         */
        boolean isNegative() {
            return negative;
        }
    }

    /**
     * A parsed AIA response and its expiration time.
     */
    static final class Entry {
        private final List<X509Certificate> certificates;
        private final long expiresAtInMillis;
        private final long generation;

        /**
         * Creates an uncached entry. The cache assigns its generation when the entry is published.
         *
         * @param certificates the parsed response certificates
         * @param expiresAtInMillis the expiration time in epoch milliseconds
         */
        Entry(List<X509Certificate> certificates, long expiresAtInMillis) {
            this(certificates, expiresAtInMillis, 0L);
        }

        private Entry(List<X509Certificate> certificates, long expiresAtInMillis, long generation) {
            this.certificates = Objects.requireNonNull(certificates, "certificates cannot be null");
            this.expiresAtInMillis = expiresAtInMillis;
            this.generation = generation;
        }

        private boolean isExpired(long now) {
            return now >= expiresAtInMillis;
        }

        private Entry withGeneration(long generation) {
            return new Entry(certificates, expiresAtInMillis, generation);
        }
    }

    private static final class SuppressionKey {
        private final String url;
        private final Object targetIdentity;

        private SuppressionKey(String url, Object targetIdentity) {
            this.url = Objects.requireNonNull(url, "url cannot be null");
            this.targetIdentity = Objects.requireNonNull(targetIdentity, "targetIdentity cannot be null");
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SuppressionKey)) {
                return false;
            }
            SuppressionKey other = (SuppressionKey) obj;
            return url.equals(other.url) && targetIdentity.equals(other.targetIdentity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(url, targetIdentity);
        }
    }

    private static final class Suppression {
        private final long generation;
        private final long expiresAtInMillis;

        private Suppression(long generation, long expiresAtInMillis) {
            this.generation = generation;
            this.expiresAtInMillis = expiresAtInMillis;
        }

        private boolean isExpired(long now) {
            return now >= expiresAtInMillis;
        }
    }
}
