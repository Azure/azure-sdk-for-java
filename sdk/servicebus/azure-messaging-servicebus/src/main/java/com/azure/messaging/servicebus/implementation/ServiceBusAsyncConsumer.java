// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.messaging.servicebus.MessageLockToken;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * A package-private consumer responsible for reading {@link ServiceBusMessage} from a specific Service Bus.
 */
public class ServiceBusAsyncConsumer implements AutoCloseable {
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ServiceBusReceiveLinkProcessor amqpReceiveLinkProcessor;
    private final MessageSerializer messageSerializer;
    private final ServiceBusMessageProcessor processor;
    private final String linkName;

    public ServiceBusAsyncConsumer(String linkName, ServiceBusReceiveLinkProcessor amqpReceiveLinkProcessor,
        MessageSerializer messageSerializer, boolean isAutoComplete, boolean autoLockRenewal,
        Duration maxAutoLockRenewDuration, AmqpRetryOptions retryOptions, MessageLockContainer messageLockContainer,
        Function<MessageLockToken, Mono<Void>> onComplete,
        Function<MessageLockToken, Mono<Void>> onAbandon,
        Function<MessageLockToken, Mono<Instant>> onRenewLock) {

        this.linkName = linkName;
        this.amqpReceiveLinkProcessor = amqpReceiveLinkProcessor;
        this.messageSerializer = messageSerializer;

        this.processor = amqpReceiveLinkProcessor
            .map(message -> this.messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .subscribeWith(new ServiceBusMessageProcessor(isAutoComplete, autoLockRenewal, maxAutoLockRenewDuration,
                retryOptions, messageLockContainer, amqpReceiveLinkProcessor.getErrorContext(),
                onComplete, onAbandon, onRenewLock));
    }

    public String getLinkName() {
        return linkName;
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            processor.onComplete();
            amqpReceiveLinkProcessor.cancel();
        }
    }

    /**
     * Begin consuming events until there are no longer any subscribers.
     *
     * @return A stream of events received from the partition.
     */
    public Flux<ServiceBusReceivedMessage> receive() {
        return processor;
    }
}
