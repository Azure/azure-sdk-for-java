// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import reactor.core.publisher.Mono;

// Temporary type to support connection cache either in new or legacy stack.
final class ConnectionCacheWrapper {
    private final boolean isNewStack;
    private final ReactorConnectionCache<ServiceBusReactorAmqpConnection> cache;
    private final ServiceBusConnectionProcessor processor;

    ConnectionCacheWrapper(ReactorConnectionCache<ServiceBusReactorAmqpConnection> cache) {
        this.isNewStack = true;
        this.cache = cache;
        this.processor = null;
    }

    ConnectionCacheWrapper(ServiceBusConnectionProcessor processor) {
        this.isNewStack = false;
        this.processor = processor;
        this.cache = null;
    }

    boolean isNewStack() {
        return isNewStack;
    }

    Mono<ServiceBusAmqpConnection> getConnection() {
        return isNewStack ? cache.get().cast(ServiceBusAmqpConnection.class) : processor;
    }

    String getFullyQualifiedNamespace() {
        return isNewStack ?  cache.getFullyQualifiedNamespace() : processor.getFullyQualifiedNamespace();
    }

    AmqpRetryOptions getRetryOptions() {
        return isNewStack ? cache.getRetryOptions() : processor.getRetryOptions();
    }

    boolean isChannelClosed() {
        return isNewStack ? cache.isCurrentConnectionClosed() : processor.isChannelClosed();
    }
}
