// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A temporary type to side by side support {@link RequestResponseChannel} caching that
 * <ul>
 *     <li>uses {@link AmqpChannelProcessor} in v2 mode without "com.azure.core.amqp.internal.session-channel-cache.v2"
 *     opt-in or in v1 mode,</li>
 *     <li>or uses {@link RequestResponseChannelCache} when "com.azure.core.amqp.internal.session-channel-cache.v2"
 *     is explicitly opted-in in v2 mode.</li>
 * </ul>
 * <p>
 * TODO (anu): remove this temporary type when removing v1 and 'RequestResponseChannelCache' is no longer opt-in for v2.
 * </p>
 */
final class ChannelCacheWrapper {
    private final AmqpChannelProcessor<RequestResponseChannel> channelProcessor;
    private final RequestResponseChannelCache channelCache;

    /**
     * Creates channel cache for V1 client or V2 client without "com.azure.core.amqp.internal.session-channel-cache.v2"
     * opted-in.
     *
     * @param channelProcessor the channel processor for caching {@link RequestResponseChannel}.
     */
    ChannelCacheWrapper(AmqpChannelProcessor<RequestResponseChannel> channelProcessor) {
        this.channelProcessor = Objects.requireNonNull(channelProcessor, "'channelProcessor' cannot be null.");
        this.channelCache = null;
    }

    /**
     * Creates channel cache for V1 client with "com.azure.core.amqp.internal.session-channel-cache.v2" opted-in.
     *
     * @param channelCache the cache for {@link RequestResponseChannel}.
     */
    ChannelCacheWrapper(RequestResponseChannelCache channelCache) {
        this.channelCache = Objects.requireNonNull(channelCache, "'channelCache' cannot be null.");
        this.channelProcessor = null;
    }

    Mono<RequestResponseChannel> get() {
        if (channelCache != null) {
            return channelCache.get();
        } else {
            return channelProcessor;
        }
    }

    Mono<Void> closeAsync() {
        if (channelCache != null) {
            return channelCache.closeAsync();
        } else {
            return channelProcessor.flatMap(RequestResponseChannel::closeAsync);
        }
    }
}
