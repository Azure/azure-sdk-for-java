// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.core.amqp.implementation.RetryUtil.withRetry;

/**
 * Temporary type to support connection cache either in v1 or v2 stack.
 * v2 underlying connection cache is {@link ReactorConnectionCache}
 * v1 underlying connection cache is {@link EventHubConnectionProcessor}
 */
final class ConnectionCacheWrapper implements Disposable {
    private final boolean isV2;
    private final ReactorConnectionCache<EventHubReactorAmqpConnection> cache;
    private final EventHubConnectionProcessor processor;

    ConnectionCacheWrapper(ReactorConnectionCache<EventHubReactorAmqpConnection> cache) {
        this.isV2 = true;
        this.cache = Objects.requireNonNull(cache, "'cache' cannot be null.");
        this.processor = null;
    }

    ConnectionCacheWrapper(EventHubConnectionProcessor processor) {
        this.isV2 = false;
        this.processor = Objects.requireNonNull(processor, "'processor' cannot be null.");
        this.cache = null;
    }

    boolean isV2() {
        return isV2;
    }

    Mono<EventHubAmqpConnection> getConnection() {
        return isV2 ? cache.get().cast(EventHubAmqpConnection.class) : processor;
    }

    String getFullyQualifiedNamespace() {
        return isV2 ?  cache.getFullyQualifiedNamespace() : processor.getFullyQualifiedNamespace();
    }

    String getEventHubName() {
        return isV2 ? cache.getEntityPath() : processor.getEventHubName();
    }

    AmqpRetryOptions getRetryOptions() {
        return isV2 ? cache.getRetryOptions() : processor.getRetryOptions();
    }

    boolean isChannelClosed() {
        return isV2 ? cache.isCurrentConnectionClosed() : processor.isChannelClosed();
    }

    Mono<EventHubManagementNode> getManagementNodeWithRetries() {
        if (isV2) {
            final AmqpRetryOptions retryOptions = cache.getRetryOptions();
            return withRetry(cache.get().flatMap(EventHubReactorAmqpConnection::getManagementNode),
                retryOptions, "Time out creating management node.");
        } else {
            return processor.getManagementNodeWithRetries();
        }
    }

    @Override
    public boolean isDisposed() {
        return isV2 ? cache.isDisposed() : processor.isDisposed();
    }

    @Override
    public void dispose() {
        if (isV2) {
            cache.dispose();
        } else {
            processor.dispose();
        }
    }
}
