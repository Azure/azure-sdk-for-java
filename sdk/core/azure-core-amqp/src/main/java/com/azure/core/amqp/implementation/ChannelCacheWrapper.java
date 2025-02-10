// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A temporary type to wrap {@link RequestResponseChannel} caching using {@link RequestResponseChannelCache}
 * TODO (anu): remove this temporary type when removing v1 and 'RequestResponseChannelCache' is no longer opt-in for v2.
 */
public final class ChannelCacheWrapper {
    private final RequestResponseChannelCache channelCache;

    /**
     * Creates channel cache for V1 client with "com.azure.core.amqp.cache" opted-in.
     *
     * @param channelCache the cache for {@link RequestResponseChannel}.
     */
    public ChannelCacheWrapper(RequestResponseChannelCache channelCache) {
        this.channelCache = Objects.requireNonNull(channelCache, "'channelCache' cannot be null.");
    }

    /**
     * Gets the underlying cache as Mono.
     *
     * @return the cache as Mono.
     */
    public Mono<RequestResponseChannel> get() {
        return channelCache.get();
    }

    /**
     * Closes the cache.
     *
     * @return Mono that completes when cache is closed.
     */
    public Mono<Void> closeAsync() {
        return channelCache.closeAsync();
    }

    /**
     * Disposes the cache.
     */
    public void dispose() {
        channelCache.dispose();
    }
}
