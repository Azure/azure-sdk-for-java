// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateHandler;
import reactor.netty.ConnectionObserver;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class defers supplying a channel pipeline with an idle timeout handler.
 */
public class DeferredIdleStateHandlerProvider implements Function<Bootstrap, BiConsumer<ConnectionObserver, Channel>> {
    public static final String HANDLER_NAME = "azureIdleStateHandler";

    private final Duration readTimeout;
    private final Duration writeTimeout;

    public DeferredIdleStateHandlerProvider(Duration readTimeout, Duration writeTimeout) {
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
    }

    @Override
    public BiConsumer<ConnectionObserver, Channel> apply(Bootstrap bootstrap) {
        IdleStateHandler idleStateHandler = new AzureIdleStateHandler(readTimeout, writeTimeout);

        return (connectionObserver, channel) -> channel.pipeline().addFirst(HANDLER_NAME, idleStateHandler);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DeferredIdleStateHandlerProvider)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        DeferredIdleStateHandlerProvider other = (DeferredIdleStateHandlerProvider) obj;
        return Objects.equals(readTimeout, other.readTimeout) && Objects.equals(writeTimeout, other.writeTimeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readTimeout, writeTimeout);
    }
}
