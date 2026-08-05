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
import java.util.function.LongSupplier;

/**
 * A bounded, access-ordered cache for AIA resolution results.
 *
 * <p>Completed entries are guarded by short synchronized sections. Loaders always run outside that lock, and
 * concurrent misses for the same URL share one in-flight result without blocking loads for other URLs.
 */
final class AiaResponseCache {
    private final int maximumSize;
    private final LongSupplier clock;
    private final Map<String, Entry> entries = new LinkedHashMap<>(16, 0.75f, true);
    private final ConcurrentHashMap<String, CompletableFuture<Entry>> inFlight = new ConcurrentHashMap<>();

    AiaResponseCache(int maximumSize, LongSupplier clock) {
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("maximumSize must be greater than zero");
        }
        this.maximumSize = maximumSize;
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    List<X509Certificate> getOrLoad(String url, Loader loader) {
        return getOrLoad(url, loader, () -> {
        });
    }

    List<X509Certificate> getOrLoad(String url, Loader loader, Runnable cacheHitAction) {
        Objects.requireNonNull(url, "url cannot be null");
        Objects.requireNonNull(loader, "loader cannot be null");
        Objects.requireNonNull(cacheHitAction, "cacheHitAction cannot be null");
        Entry cached = getIfFresh(url);
        if (cached != null) {
            cacheHitAction.run();
            return cached.certificates;
        }

        CompletableFuture<Entry> created = new CompletableFuture<>();
        CompletableFuture<Entry> existing = inFlight.putIfAbsent(url, created);
        if (existing != null) {
            return await(existing).certificates;
        }

        try {
            Entry rechecked = getIfFresh(url);
            Entry result = rechecked != null ? rechecked : Objects.requireNonNull(loader.load(), "loader result");
            if (rechecked != null) {
                cacheHitAction.run();
            }
            if (rechecked == null) {
                putIfFresh(url, result);
            }
            created.complete(result);
            return result.certificates;
        } catch (RuntimeException e) {
            created.completeExceptionally(e);
            return created.join().certificates;
        } finally {
            if (!created.isDone()) {
                created.completeExceptionally(
                    new IllegalStateException("AIA resolution terminated before producing a result"));
            }
            inFlight.remove(url, created);
        }
    }

    synchronized void clear() {
        entries.clear();
    }

    synchronized int size() {
        removeExpiredEntries(clock.getAsLong());
        return entries.size();
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

    private synchronized void putIfFresh(String url, Entry entry) {
        long now = clock.getAsLong();
        removeExpiredEntries(now);
        if (entry.isExpired(now)) {
            return;
        }

        entries.put(url, entry);
        while (entries.size() > maximumSize) {
            Iterator<String> iterator = entries.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
    }

    private void removeExpiredEntries(long now) {
        entries.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
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
        Entry load();
    }

    static final class Entry {
        private final List<X509Certificate> certificates;
        private final long expiresAtInMillis;

        Entry(List<X509Certificate> certificates, long expiresAtInMillis) {
            this.certificates = Objects.requireNonNull(certificates, "certificates cannot be null");
            this.expiresAtInMillis = expiresAtInMillis;
        }

        private boolean isExpired(long now) {
            return now >= expiresAtInMillis;
        }
    }
}
