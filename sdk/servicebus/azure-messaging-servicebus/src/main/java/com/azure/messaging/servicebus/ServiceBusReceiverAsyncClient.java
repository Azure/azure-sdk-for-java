// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusAsyncConsumer;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLinkProcessor;
import com.azure.messaging.servicebus.models.ReceiveMessageOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.Closeable;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An <b>asynchronous</b> receiver responsible for receiving {@link ServiceBusMessage} from a specific queue or topic.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusReceiverAsyncClient implements Closeable {
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClient.class);
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final MessageSerializer messageSerializer;
    private final Serializable maxAutoRenewDuration;
    private final int prefetch;
    private final boolean isAutoComplete;
    private final ReceiveMode receiveMode;

    // Client will maintain the sequence number of last peeked message.
    //private long lastPeekedSequenceNumber = 0;

    /**
     * Map containing linkNames and their associated consumers. Key: linkName Value: consumer associated with that
     * linkName.
     */
    private final ConcurrentHashMap<String, ServiceBusAsyncConsumer> openConsumers = new ConcurrentHashMap<>();

    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath,
        ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, ReceiveMessageOptions receiveMessageOptions) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.entityPath = entityPath;
        this.connectionProcessor = connectionProcessor;
        this.messageSerializer = messageSerializer;
        this.prefetch = receiveMessageOptions.getPrefetchCount();
        this.maxAutoRenewDuration = receiveMessageOptions.getMaxAutoRenewDuration();
        this.isAutoComplete = receiveMessageOptions.isAutoComplete();
        this.receiveMode = receiveMessageOptions.getReceiveMode();
    }

    /**
     * Gets the fully qualified Service Bus namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Service Bus namespace that the connection is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the Service Bus resource this client interacts with.
     *
     * @return The Service Bus resource this client interacts with.
     */
    public String getServiceBusResourceName() {
        return entityPath;
    }

    /**
     * Receives a stream of {@link ServiceBusReceivedMessage}.
     *
     * @return A stream of messages from Service Bus.
     */
    public Flux<ServiceBusReceivedMessage> receive() {
        if (isDisposed.get()) {
            return Flux.error(new IllegalStateException("Cannot receive from a client that is already closed."));
        }

        //TODO (conniey): This will return the same link because the linkName is not unique. Is this what we want?
        final String linkName = entityPath;
        return openConsumers.computeIfAbsent(entityPath, name -> {
            logger.info("{}: Creating consumer for link '{}'", entityPath, linkName);
            return createServiceBusConsumer(linkName);
        })
            .receive()
            .doOnCancel(() -> removeLink(linkName, SignalType.CANCEL))
            .doOnComplete(() -> removeLink(linkName, SignalType.ON_COMPLETE))
            .doOnError(error -> removeLink(linkName, SignalType.ON_ERROR));
    }

    /**
     * Disposes of the consumer by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        final ArrayList<String> keys = new ArrayList<>(openConsumers.keySet());
        for (String key : keys) {
            removeLink(key, SignalType.ON_COMPLETE);
        }

        connectionProcessor.dispose();
    }

    /**
     * Abandon {@link ServiceBusMessage} with lock token and updated message property. This will make the message
     * available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param message to be used.
     * @param propertiesToModify Message properties to modify.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> abandon(ServiceBusReceivedMessage message, Map<String, Object> propertiesToModify) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Abandon {@link ServiceBusMessage} with lock token. This will make the message available again for processing.
     * Abandoning a message will increase the delivery count on the message.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> abandon(ServiceBusReceivedMessage message) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Completes a {@link ServiceBusMessage} using its lock token. This will delete the message from the service.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> complete(ServiceBusReceivedMessage message) {
        logger.info("Completing message: {}", message.getSequenceNumber());
        //TODO(feature-to-implement)
        return Mono.empty();
    }

    /**
     * Defers a {@link ServiceBusMessage} using its lock token with modified message property. This will move message
     * into deferred subqueue.
     *
     * @param message to be used.
     * @param propertiesToModify Message properties to modify.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message, Map<String, Object> propertiesToModify) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Defers a {@link ServiceBusMessage} using its lock token. This will move message into deferred subqueue.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Moves a {@link ServiceBusMessage} to the deadletter sub-queue.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Moves a {@link ServiceBusMessage} to the deadletter sub-queue with deadletter reason, error description and
     * modifided properties.
     *
     * @param message to be used.
     * @param deadLetterReason The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @param propertiesToModify Message properties to modify.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message, String deadLetterReason,
        String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Moves a {@link ServiceBusMessage} to the deadletter sub-queue with deadletter reason and error description.
     *
     * @param message to be used.
     * @param deadLetterReason The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message, String deadLetterReason,
        String deadLetterErrorDescription) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Moves a {@link ServiceBusMessage} to the deadletter sub-queue with modified message properties.
     *
     * @param message to be used.
     * @param propertiesToModify Message properties to modify.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message, Map<String, Object> propertiesToModify) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on the
     * setting specified on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode, the message is
     * locked on the server for this receiver instance for a duration as specified during the Queue creation
     * (LockDuration). If processing of the message requires longer than this duration, the lock needs to be renewed.
     * For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Instant renewMessageLock(ServiceBusReceivedMessage message) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Receives a deferred {@link ServiceBusMessage}. Deferred messages can only be received by using sequence number.
     *
     * @param sequenceNumber The {@link ServiceBusReceivedMessage#getSequenceNumber()}.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Peek single message on Service Bus Queue or Subscriber.
     *
     * @return Single {@link ServiceBusReceivedMessage} .
     */
    public Mono<ServiceBusReceivedMessage> inspectMessage() {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode())
            .flatMap(ServiceBusManagementNode::peek);
    }

    /**
     * Peek single message on Service Bus Queue or Subscriber.
     *
     * @param fromSequenceNumber to peek message from.
     *
     * @return Single {@link ServiceBusReceivedMessage} .
     */
    public Mono<ServiceBusReceivedMessage> inspectMessage(int fromSequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode())
            .flatMap(ServiceBusManagementNode::peek);
    }

    /**
     * @param maxMessages to peek.
     *
     * @return Flux of {@link ServiceBusReceivedMessage}.
     */
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages) {
        return null;
    }

    /**
     * @param maxMessages to peek.
     * @param fromSequenceNumber to peek message from.
     *
     * @return Flux of {@link ServiceBusReceivedMessage}.
     */
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages, long fromSequenceNumber) {
        return null;
    }

    private void removeLink(String linkName, SignalType signalType) {
        logger.info("{}: Receiving completed. Signal[{}]", linkName, signalType);

        final ServiceBusAsyncConsumer removed = openConsumers.remove(linkName);
        if (removed != null) {
            try {
                removed.close();
            } catch (Throwable e) {
                logger.warning("[{}][{}]: Error occurred while closing consumer '{}'",
                    fullyQualifiedNamespace, entityPath, linkName, e);
            }
        }
    }

    private ServiceBusAsyncConsumer createServiceBusConsumer(String linkName) {
        final Flux<AmqpReceiveLink> receiveLinkMono =
            connectionProcessor.flatMap(connection ->
                connection.createReceiveLink(linkName, entityPath, receiveMode))
                .doOnNext(next -> logger.verbose("Created consumer for Service Bus resource: {}", next.getEntityPath()))
                .repeat();

        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
        final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLinkMono.subscribeWith(
            new ServiceBusReceiveLinkProcessor(prefetch, retryPolicy, connectionProcessor));

        return new ServiceBusAsyncConsumer(linkMessageProcessor, messageSerializer, isAutoComplete, this::complete);
    }
}
