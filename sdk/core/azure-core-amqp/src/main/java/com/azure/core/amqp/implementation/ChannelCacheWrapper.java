// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A temporary type to side by side support {@link RequestResponseChannel} caching that
 * <ul>
 *     <li>uses {@link AmqpChannelProcessor} in v2 mode without "com.azure.core.amqp.cache"
 *     opt-in or in v1 mode,</li>
 *     <li>or uses {@link RequestResponseChannelCache} when "com.azure.core.amqp.cache"
 *     is explicitly opted-in in v2 mode.</li>
 * </ul>
 * <p>
 * TODO (anu): remove this temporary type when removing v1 and 'RequestResponseChannelCache' is no longer opt-in for v2.
 * </p>
 */
public final class ChannelCacheWrapper {
    private final Mono<RequestResponseChannel> channelProcessor;
    private final RequestResponseChannelCache channelCache;

    /**
     * Creates channel cache for V1 client or V2 client without "com.azure.core.amqp.cache"
     * opted-in.
     *
     * @param channelProcessor the {@link AmqpChannelProcessor} for caching {@link RequestResponseChannel}.
     */
    public ChannelCacheWrapper(Mono<RequestResponseChannel> channelProcessor) {
        this.channelProcessor = Objects.requireNonNull(channelProcessor, "'channelProcessor' cannot be null.");
        this.channelCache = null;
    }

    /**
     * Creates channel cache for V1 client with "com.azure.core.amqp.cache" opted-in.
     *
     * @param channelCache the cache for {@link RequestResponseChannel}.
     */
    public ChannelCacheWrapper(RequestResponseChannelCache channelCache) {
        this.channelCache = Objects.requireNonNull(channelCache, "'channelCache' cannot be null.");
        this.channelProcessor = null;
    }

    /**
     * Gets the underlying cache as Mono.
     *
     * @return the cache as Mono.
     */
    public Mono<RequestResponseChannel> get() {
        if (channelCache != null) {
            return channelCache.get();
        } else {
            return channelProcessor;
        }
    }

    /**
     * Closes the cache.
     *
     * @return Mono that completes when cache is closed.
     */
    public Mono<Void> closeAsync() {
        if (channelCache != null) {
            return channelCache.closeAsync();
        } else {
            return channelProcessor.flatMap(RequestResponseChannel::closeAsync);
        }
    }

    /**
     * Disposes the cache.
     */
    public void dispose() {
        if (channelCache != null) {
            channelCache.dispose();
        } else {
            if (channelProcessor instanceof Disposable) {
                // Invokes AmqpChannelProcessor::dispose
                ((Disposable) channelProcessor).dispose();
            }
        }
    }
}
