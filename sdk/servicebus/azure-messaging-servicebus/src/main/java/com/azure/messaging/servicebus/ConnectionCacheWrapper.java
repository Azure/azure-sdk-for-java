// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import reactor.core.publisher.Mono;

// Temporary type to support connection cache either in v1 or v2 stack.
final class ConnectionCacheWrapper {
    private final boolean isV2;
    private final ReactorConnectionCache<ServiceBusReactorAmqpConnection> cache;
    private final ServiceBusConnectionProcessor processor;

    ConnectionCacheWrapper(ReactorConnectionCache<ServiceBusReactorAmqpConnection> cache) {
        this.isV2 = true;
        this.cache = cache;
        this.processor = null;
    }

    ConnectionCacheWrapper(ServiceBusConnectionProcessor processor) {
        this.isV2 = false;
        this.processor = processor;
        this.cache = null;
    }

    boolean isV2() {
        return isV2;
    }

    Mono<ServiceBusAmqpConnection> getConnection() {
        return isV2 ? cache.get().cast(ServiceBusAmqpConnection.class) : processor;
    }

    String getFullyQualifiedNamespace() {
        return isV2 ?  cache.getFullyQualifiedNamespace() : processor.getFullyQualifiedNamespace();
    }

    AmqpRetryOptions getRetryOptions() {
        return isV2 ? cache.getRetryOptions() : processor.getRetryOptions();
    }

    boolean isChannelClosed() {
        return isV2 ? cache.isCurrentConnectionClosed() : processor.isChannelClosed();
    }
}
