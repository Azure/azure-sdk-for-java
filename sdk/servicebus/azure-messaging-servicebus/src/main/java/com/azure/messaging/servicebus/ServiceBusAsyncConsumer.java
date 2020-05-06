// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageManagementOperations;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.ServiceBusMessageProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A package-private consumer responsible for reading {@link ServiceBusMessage} from a specific Service Bus link.
 */
class ServiceBusAsyncConsumer implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(ServiceBusAsyncConsumer.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final String linkName;
    private final ServiceBusReceiveLinkProcessor linkProcessor;
    private final MessageSerializer messageSerializer;
    private final ServiceBusMessageProcessor processor;

    ServiceBusAsyncConsumer(String linkName, ServiceBusReceiveLinkProcessor linkProcessor,
        MessageSerializer messageSerializer, boolean isAutoComplete, boolean autoLockRenewal,
        Duration maxAutoLockRenewDuration, AmqpRetryOptions retryOptions,
        BiFunction<MessageLockToken, String, Mono<Instant>> renewMessageLock) {
        this.linkName = linkName;
        this.linkProcessor = linkProcessor;
        this.messageSerializer = messageSerializer;

        final MessageManagement messageManagement = new MessageManagement(linkProcessor, renewMessageLock);

        this.processor = linkProcessor
            .map(message -> this.messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .subscribeWith(new ServiceBusMessageProcessor(linkName, isAutoComplete, autoLockRenewal,
                maxAutoLockRenewDuration, retryOptions, linkProcessor.getErrorContext(), messageManagement));
    }

    /**
     * Gets the receive link name.
     *
     * @return The receive link name for the consumer.
     */
    String getLinkName() {
        return linkName;
    }

    /**
     * Begin consuming events until there are no longer any subscribers.
     *
     * @return A stream of events received from the partition.
     */
    Flux<ServiceBusReceivedMessage> receive() {
        return processor;
    }

    Mono<Void> updateDisposition(String lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {

        final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
            deadLetterErrorDescription, propertiesToModify);

        if (deliveryState == null) {
            return monoError(logger,
                new IllegalArgumentException("'dispositionStatus' is not known. status: " + dispositionStatus));
        }

        return linkProcessor.updateDisposition(lockToken, deliveryState);
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            processor.onComplete();
            linkProcessor.cancel();
        }
    }

    private static final class MessageManagement implements MessageManagementOperations {
        private final ServiceBusReceiveLinkProcessor link;
        private final BiFunction<MessageLockToken, String, Mono<Instant>> renewMessageLock;

        private MessageManagement(ServiceBusReceiveLinkProcessor link,
            BiFunction<MessageLockToken, String, Mono<Instant>> renewMessageLock) {
            this.link = link;
            this.renewMessageLock = renewMessageLock;
        }

        @Override
        public Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
            return link.updateDisposition(lockToken, deliveryState);
        }

        @Override
        public Mono<Instant> renewMessageLock(String lockToken, String associatedLinkName) {
            return renewMessageLock.apply(MessageLockToken.fromString(lockToken), associatedLinkName);
        }
    }
}
