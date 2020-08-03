// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import reactor.netty.ConnectionObserver;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class defers supplying a channel pipeline with a timeout handler.
 */
public class DeferredTimeoutProvider implements Function<Bootstrap, BiConsumer<ConnectionObserver, Channel>> {
    private final Duration writeTimeout;
    private final Duration responseTimeout;
    private final Duration readTimeout;

    public static final String NAME = "azure.timeoutHandler";

    public DeferredTimeoutProvider(Duration writeTimeout, Duration responseTimeout, Duration readTimeout) {
        this.writeTimeout = writeTimeout;
        this.responseTimeout = responseTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public BiConsumer<ConnectionObserver, Channel> apply(Bootstrap bootstrap) {
        return (connectionObserver, channel) -> channel.pipeline()
            .addFirst(NAME, new TimeoutHandler(writeTimeout, responseTimeout, readTimeout));
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

        return Objects.equals(writeTimeout, other.writeTimeout)
            && Objects.equals(responseTimeout, other.responseTimeout)
            && Objects.equals(readTimeout, other.readTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(writeTimeout, responseTimeout, readTimeout);
    }
}
