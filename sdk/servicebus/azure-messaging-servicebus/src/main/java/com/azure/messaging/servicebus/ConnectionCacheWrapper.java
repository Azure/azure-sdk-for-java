// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import reactor.core.publisher.Mono;

/**
 * Temporary type to support connection cache either in v1 or v2 stack.
 * v2 underlying connection cache is {@link ReactorConnectionCache}
 */
final class ConnectionCacheWrapper {
    private final boolean isV2;
    private final ReactorConnectionCache<ServiceBusReactorAmqpConnection> cache;

    ConnectionCacheWrapper(ReactorConnectionCache<ServiceBusReactorAmqpConnection> cache) {
        this.isV2 = true;
        this.cache = cache;
    }

    boolean isV2() {
        return isV2;
    }

    Mono<ServiceBusAmqpConnection> getConnection() {
        return cache.get().cast(ServiceBusAmqpConnection.class);
    }

    String getFullyQualifiedNamespace() {
        return cache.getFullyQualifiedNamespace();
    }

    AmqpRetryOptions getRetryOptions() {
        return cache.getRetryOptions();
    }

    boolean isChannelClosed() {
        return cache.isCurrentConnectionClosed();
    }
}
