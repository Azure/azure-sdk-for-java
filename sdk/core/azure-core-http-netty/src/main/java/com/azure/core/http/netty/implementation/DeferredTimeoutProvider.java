// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import reactor.netty.ConnectionObserver;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class defers supplying a channel pipeline with a timeout handler.
 */
public class DeferredTimeoutProvider implements Function<Bootstrap, BiConsumer<ConnectionObserver, Channel>> {
    private final long writeTimeoutNanos;
    private final long responseTimeoutNanos;
    private final long readTimeoutNanos;

    public static final String NAME = "azure.timeoutHandler";

    public DeferredTimeoutProvider(long writeTimeoutNanos, long responseTimeoutNanos, long readTimeoutNanos) {
        this.writeTimeoutNanos = writeTimeoutNanos;
        this.responseTimeoutNanos = responseTimeoutNanos;
        this.readTimeoutNanos = readTimeoutNanos;
    }

    @Override
    public BiConsumer<ConnectionObserver, Channel> apply(Bootstrap bootstrap) {
        return (connectionObserver, channel) -> channel.pipeline()
            .addFirst(NAME, new TimeoutHandler(writeTimeoutNanos, responseTimeoutNanos, readTimeoutNanos));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DeferredTimeoutProvider)) {
            return false;
        }

        DeferredTimeoutProvider other = (DeferredTimeoutProvider) o;

        return writeTimeoutNanos == other.writeTimeoutNanos
            && responseTimeoutNanos == other.responseTimeoutNanos
            && readTimeoutNanos == other.readTimeoutNanos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(writeTimeoutNanos, responseTimeoutNanos, readTimeoutNanos);
    }
}
