// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * A package-private consumer responsible for reading {@link ServiceBusMessage} from a specific Service Bus link.
 */
public class ServiceBusAsyncConsumer implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(ServiceBusAsyncConsumer.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final String linkName;
    private final ServiceBusReceiveLinkProcessor amqpReceiveLinkProcessor;
    private final MessageSerializer messageSerializer;
    private final ServiceBusMessageProcessor processor;

    public ServiceBusAsyncConsumer(String linkName, ServiceBusReceiveLinkProcessor linkProcessor,
        Mono<ServiceBusManagementNode> managementNode, MessageSerializer messageSerializer, boolean isAutoComplete,
        boolean autoLockRenewal, Duration maxAutoLockRenewDuration, AmqpRetryOptions retryOptions) {
        this.linkName = linkName;
        this.amqpReceiveLinkProcessor = linkProcessor;
        this.messageSerializer = messageSerializer;

        final MessageManagement messageManagement = new MessageManagement(linkProcessor, managementNode);

        this.processor = linkProcessor
            .map(message -> this.messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .subscribeWith(new ServiceBusMessageProcessor(linkName, isAutoComplete, autoLockRenewal,
                maxAutoLockRenewDuration, retryOptions, linkProcessor.getErrorContext(), messageManagement));
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

    public Mono<Void> updateDisposition(String lockToken, DispositionStatus dispositionStatus, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {

        final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
            deadLetterErrorDescription, propertiesToModify);

        if (deliveryState == null) {
            return monoError(logger,
                new IllegalArgumentException("'dispositionStatus' is not known. status: " + dispositionStatus));
        }

        return amqpReceiveLinkProcessor.updateDisposition(lockToken, deliveryState);
    }

    /**
     * Gets the receive link name.
     *
     * @return The receive link name for the consumer.
     */
    public String getLinkName() {
        return linkName;
    }

    private static final class MessageManagement implements MessageManagementOperations {
        private final ServiceBusReceiveLinkProcessor link;
        private final Mono<ServiceBusManagementNode> node;

        private MessageManagement(ServiceBusReceiveLinkProcessor link, Mono<ServiceBusManagementNode> node) {

            this.link = link;
            this.node = node;
        }

        @Override
        public Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
            return null;
        }

        @Override
        public Mono<Instant> renewMessageLock(String lockToken, String associatedLinkName) {
            return null;
        }
    }
}
