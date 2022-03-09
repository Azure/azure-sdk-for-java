// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.logging.ClientLogger;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This class represents an immutable map used by {@link NettyToAzureCoreHttpHeadersWrapper} which has an internal
 * deferred cache.
 *
 * @param <V> Type of the value.
 */
final class DeferredCacheImmutableMap<V> extends AbstractMap<String, V> {
    private final ClientLogger logger;
    private final Map<String, V> internalCache;
    private final HttpHeaders nettyHeaders;
    private final Function<List<String>, V> getter;

    DeferredCacheImmutableMap(ClientLogger logger, Map<String, V> internalCache, HttpHeaders nettyHeaders,
        Function<List<String>, V> getter) {
        this.logger = logger;
        this.internalCache = internalCache;
        this.nettyHeaders = nettyHeaders;
        this.getter = getter;
    }

    @Override
    public int size() {
        return nettyHeaders.names().size();
    }

    @Override
    public boolean isEmpty() {
        return nettyHeaders.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return nettyHeaders.contains((String) key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw logger.logExceptionAsWarning(new UnsupportedOperationException());
    }

    @Override
    public V get(final Object key) {
        // Calling nettyHeaders.get(key) returns only the first value in the headers for the given key.
        // If there are multiple values, the user will not get the result they expect.
        // For now, this is resolved by joining the headers back into a String here, with the obvious
        // performance implication, and therefore it is recommended that users steer away from calling
        // httpHeaders.toMap().get(key), and instead be directed towards httpHeaders.get(key), as this
        // avoids the need for unnecessary string.join operations.
        return internalCache.computeIfAbsent((String) key, k -> getter.apply(nettyHeaders.getAll(k)));
    }

    @Override
    public V put(String key, V value) {
        throw logger.logExceptionAsWarning(new UnsupportedOperationException());
    }

    @Override
    public V remove(Object key) {
        throw logger.logExceptionAsWarning(new UnsupportedOperationException());
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        throw logger.logExceptionAsWarning(new UnsupportedOperationException());
    }

    @Override
    public void clear() {
        throw logger.logExceptionAsWarning(new UnsupportedOperationException());
    }

    @Override
    public Set<Entry<String, V>> entrySet() {
        return new AbstractSet<Entry<String, V>>() {
            @Override
            public Iterator<Entry<String, V>> iterator() {
                return nettyHeaders.names().stream()
                    .map(name -> (Map.Entry<String, V>) new SimpleImmutableEntry<>(name, get(name)))
                    .iterator();
            }

            @Override
            public int size() {
                return DeferredCacheImmutableMap.this.size();
            }
        };
    }
}
