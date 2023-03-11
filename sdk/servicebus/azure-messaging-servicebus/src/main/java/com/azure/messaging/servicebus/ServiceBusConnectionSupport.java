// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import reactor.core.publisher.Mono;

final class ServiceBusConnectionSupport {
    private final ServiceBusConnectionProcessor processor;
    private final ReactorConnectionCache<ServiceBusReactorAmqpConnection> cache;

    ServiceBusConnectionSupport(ServiceBusConnectionProcessor processor) {
        this.processor = processor;
        this.cache = null;
    }

    ServiceBusConnectionSupport(ReactorConnectionCache<ServiceBusReactorAmqpConnection> cache) {
        this.cache = cache;
        this.processor = null;
    }

    boolean isLegacyStack() {
        return processor != null;
    }

    String getFullyQualifiedNamespace() {
        return isLegacyStack() ? processor.getFullyQualifiedNamespace() : cache.getFullyQualifiedNamespace();
    }

    Mono<ServiceBusAmqpConnection> getConnection() {
        return isLegacyStack() ? processor : cache.get().cast(ServiceBusAmqpConnection.class);
    }


    AmqpRetryOptions getRetryOptions() {
        return isLegacyStack() ? processor.getRetryOptions() : cache.getRetryOptions();
    }

    boolean isChannelClosed() {
        return isLegacyStack() ? processor.isChannelClosed() : cache.isCurrentConnectionClosed();
    }
}
