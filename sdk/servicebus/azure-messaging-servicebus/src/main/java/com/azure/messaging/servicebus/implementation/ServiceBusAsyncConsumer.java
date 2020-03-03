// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A package-private consumer responsible for reading {@link ServiceBusMessage} from a specific Service Bus.
 */
public class ServiceBusAsyncConsumer implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(ServiceBusAsyncConsumer.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final String transferEntityPath;
    private final MessagingEntityType entityType;
    private final ServiceBusReceiveLinkProcessor amqpReceiveLinkProcessor;
    private final MessageSerializer messageSerializer;
    private final ServiceBusMessageProcessor processor;

    public ServiceBusAsyncConsumer(ServiceBusReceiveLinkProcessor amqpReceiveLinkProcessor,
        MessageSerializer messageSerializer, boolean isAutoComplete, String transferEntityPath,
        MessagingEntityType entityType, Function<ServiceBusReceivedMessage, Mono<Void>> completeFunction) {
        this.transferEntityPath = transferEntityPath;
        this.entityType = entityType;
        this.amqpReceiveLinkProcessor = amqpReceiveLinkProcessor;
        this.messageSerializer = messageSerializer;
        this.processor = amqpReceiveLinkProcessor
            .doOnSubscribe(e -> {
                logger.info("There was a subscription.");
            })
            .map(message -> this.messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .subscribeWith(new ServiceBusMessageProcessor(isAutoComplete, this::complete));
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

    /**
     * Completes a {@link ServiceBusMessage} using its lock token. This will delete the message from the service.
     *
     * @param receivedMessage to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     * @throws AmqpException if {@link ServiceBusReceivedMessage#getLockToken() a lock token} is not defined.
     */
    public Mono<Void> complete(ServiceBusReceivedMessage receivedMessage) {

        if (receivedMessage.getLockToken() == null) {
            final String message = String.format("%s: Cannot complete message without lock token. Sequence number: %s",
                entityPath, receivedMessage.getSequenceNumber());

            return Mono.error(logger.logExceptionAsWarning(new AmqpException(false, message,
                new SessionErrorContext(connectionProcessor.getFullyQualifiedNamespace(), entityPath))));
        }

        logger.info("{}: Completing message. Sequence number: {}", entityPath, receivedMessage.getSequenceNumber());

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, transferEntityPath, entityType))
            .flatMap(node -> node.complete(receivedMessage.getLockToken()));
    }
}
