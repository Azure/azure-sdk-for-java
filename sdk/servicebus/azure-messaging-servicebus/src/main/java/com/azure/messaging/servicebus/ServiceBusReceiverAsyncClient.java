// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An <b>asynchronous</b> receiver responsible for receiving {@link ServiceBusReceivedMessage} from a specific queue
 * or topic on Azure Service Bus.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusReceiverAsyncClient implements Closeable {
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClient.class);
    private final ConcurrentHashMap<UUID, Instant> lockTokenExpirationMap = new ConcurrentHashMap<>();
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final boolean isSessionEnabled;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final Duration maxAutoRenewDuration;
    private final int prefetch;
    private final boolean isAutoComplete;
    private final ReceiveMode receiveMode;

    /**
     * Map containing linkNames and their associated consumers. Key: linkName Value: consumer associated with that
     * linkName.
     */
    private final ConcurrentHashMap<String, ServiceBusAsyncConsumer> openConsumers = new ConcurrentHashMap<>();

    ServiceBusReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType,
        boolean isSessionEnabled, ReceiveMessageOptions receiveMessageOptions,
        ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider,
        MessageSerializer messageSerializer) {

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor, "'connectionProcessor' cannot be null.");
        this.tracerProvider = Objects.requireNonNull(tracerProvider, "'tracerProvider' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");

        Objects.requireNonNull(receiveMessageOptions, "'receiveMessageOptions' cannot be null.");
        this.prefetch = receiveMessageOptions.getPrefetchCount();
        this.maxAutoRenewDuration = receiveMessageOptions.getMaxAutoRenewDuration();
        this.isAutoComplete = receiveMessageOptions.isAutoComplete();
        this.receiveMode = receiveMessageOptions.getReceiveMode();

        this.entityType = entityType;
        this.isSessionEnabled = isSessionEnabled;
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
            return Flux.error(logger.logExceptionAsError(
                new IllegalStateException("Cannot receive from a client that is already closed.")));
        }

        if (receiveMode != ReceiveMode.PEEK_LOCK && isAutoComplete) {
            return Flux.error(logger.logExceptionAsError(new UnsupportedOperationException(
                "Autocomplete is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.")));
        }

        // TODO (conniey): This returns the same consumer instance because the entityPath is not unique.
        //  Python and .NET does not have the same behaviour.
        final String linkName = entityPath;
        return getOrCreateConsumer(entityPath)
            .receive()
            .map(message -> {
                if (message.getLockToken() == null || MessageUtils.ZERO_LOCK_TOKEN.equals(message.getLockToken())) {
                    return message;
                }

                lockTokenExpirationMap.compute(message.getLockToken(), (key, existing) -> {
                    if (existing == null) {
                        return message.getLockedUntil();
                    } else {
                        return existing.isBefore(message.getLockedUntil())
                            ? message.getLockedUntil()
                            : existing;
                    }
                });

                return message;
            })
            .doOnCancel(() -> removeLink(linkName, SignalType.CANCEL))
            .doOnComplete(() -> removeLink(linkName, SignalType.ON_COMPLETE))
            .doOnError(error -> removeLink(linkName, SignalType.ON_ERROR));
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
        return abandon(message, null);
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
        return updateDisposition(message, DispositionStatus.ABANDONED, null, null, propertiesToModify);
    }

    /**
     * Completes a {@link ServiceBusMessage} using its lock token. This will delete the message from the service.
     *
     * @param message Message to be completed.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> complete(ServiceBusReceivedMessage message) {
        return updateDisposition(message, DispositionStatus.COMPLETED, null, null, null);
    }

    /**
     * Defers a {@link ServiceBusMessage} using its lock token. This will move message into deferred subqueue.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> defer(ServiceBusReceivedMessage message) {
        return defer(message, null);
    }

    /**
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on the
     * setting specified on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode, the message is
     * locked on the server for this receiver instance for a duration as specified during the Queue creation
     * (LockDuration). If processing of the message requires longer than this duration, the lock needs to be renewed.
     * For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param messageLock The {@link UUID} value of the message lock to renew.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Instant> renewMessageLock(UUID messageLock) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode -> serviceBusManagementNode
                .renewMessageLock(messageLock));
    }

    /**
     * Asynchronously renews the lock on the specified message. The lock will be renewed based on the
     * setting specified on the entity. When a message is received in {@link ReceiveMode#PEEK_LOCK} mode, the message is
     * locked on the server for this receiver instance for a duration as specified during the Queue creation
     * (LockDuration). If processing of the message requires longer than this duration, the lock needs to be renewed.
     * For each renewal, the lock is reset to the entity's LockDuration value.
     *
     * @param receivedMessage to be used to renew.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Instant> renewMessageLock(ServiceBusReceivedMessage receivedMessage) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(serviceBusManagementNode -> serviceBusManagementNode
                .renewMessageLock(receivedMessage.getLockToken())
                .map(instant -> {
                    receivedMessage.setLockedUntil(instant);
                    return instant;
                }));
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
        return updateDisposition(message, DispositionStatus.DEFERRED, null, null, propertiesToModify);
    }

    /**
     * Moves a {@link ServiceBusMessage} to the deadletter sub-queue.
     *
     * @param message to be used.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> deadLetter(ServiceBusReceivedMessage message) {
        return deadLetter(message, null);
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
        return deadLetter(message, null, null, propertiesToModify);
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
        return deadLetter(message, deadLetterReason, deadLetterErrorDescription, null);
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
        return updateDisposition(message, DispositionStatus.SUSPENDED, deadLetterReason, deadLetterErrorDescription,
            propertiesToModify);
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
     * Reads the next active message without changing the state of the receiver or the message source.
     * The first call to {@code peek()} fetches the first active message for
     * this receiver. Each subsequent call fetches the subsequent message in the entity.
     *
     * @return Single {@link ServiceBusReceivedMessage} peeked.
     */
    public Mono<ServiceBusReceivedMessage> peek() {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(ServiceBusManagementNode::peek);
    }

    /**
     * Reads next the active message without changing the state of the receiver or the message source.
     *
     * @param fromSequenceNumber The sequence number from where to read the message.
     *
     * @return Single {@link ServiceBusReceivedMessage} peeked.
     */
    public Mono<ServiceBusReceivedMessage> peek(long fromSequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(node -> node.peek(fromSequenceNumber));
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @return The {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     */
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peekBatch(maxMessages));
    }

    /**
     * Reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param maxMessages The number of messages.
     * @param fromSequenceNumber The sequence number from where to read the message.
     * @return The {@link Flux} of {@link ServiceBusReceivedMessage} peeked.
     */
    public Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages, long fromSequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(node -> node.peekBatch(maxMessages, fromSequenceNumber));
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

    private Mono<Boolean> isLockTokenValid(UUID lockToken) {
        final Instant lockedUntilUtc = lockTokenExpirationMap.get(lockToken);
        if (lockedUntilUtc == null) {
            logger.warning("lockToken[{}] is not owned by this receiver.", lockToken);
            return Mono.just(false);
        }

        //TODO (conniey): This check is done locally in Track 1. It's possible there is server skew.
        // who knows how reliable this is.
        final Instant now = Instant.now();
        if (lockedUntilUtc.isBefore(now)) {
            return Mono.error(logger.logExceptionAsError(new AmqpException(false, String.format(
                "Lock already expired for the lock token. Expiration: '%s'. Now: '%s'", lockedUntilUtc, now),
                getErrorContext())));
        }

        return Mono.just(true);
    }

    private Mono<Void> updateDisposition(ServiceBusReceivedMessage message, DispositionStatus dispositionStatus,
        String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {
        if (message == null) {
            return Mono.error(new NullPointerException("'message' cannot be null."));
        }

        final UUID lockToken = message.getLockToken();
        if (receiveMode != ReceiveMode.PEEK_LOCK) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                "'%s' is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.", dispositionStatus))));
        } else if (lockToken == null) {
            return Mono.error(logger.logExceptionAsError(new IllegalArgumentException(
                "'message.getLockToken()' cannot be null.")));
        }

        final Instant instant = lockTokenExpirationMap.get(lockToken);
        logger.info("{}: Update started. Disposition: {}. Sequence number: {}. Lock: {}. Expiration: {}",
            entityPath, dispositionStatus, message.getSequenceNumber(), lockToken, instant);

        return isLockTokenValid(lockToken).flatMap(isLocked -> {
            return connectionProcessor.flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMap(node -> {
                    if (isLocked) {
                        return node.updateDisposition(lockToken, dispositionStatus, deadLetterReason,
                            deadLetterErrorDescription, propertiesToModify);
                    } else {
                        //TODO (conniey): in Track 1, I believe there was a way to do this.
                        return Mono.error(
                            new UnsupportedOperationException("Cannot complete a message that is not locked."));
                    }
                });
        }).then(Mono.fromRunnable(() -> {
            logger.info("{}: Update completed. Disposition: {}. Sequence number: {}. Lock: {}.",
                entityPath, dispositionStatus, message.getSequenceNumber(), lockToken);

            lockTokenExpirationMap.remove(lockToken);
        }));
    }

    private ServiceBusAsyncConsumer getOrCreateConsumer(String linkName) {
        return openConsumers.computeIfAbsent(linkName, name -> {
            logger.info("{}: Creating consumer for link '{}'", entityPath, linkName);

            final Flux<AmqpReceiveLink> receiveLink =
                connectionProcessor.flatMap(connection -> connection.createReceiveLink(linkName, entityPath,
                    receiveMode, isSessionEnabled, null, entityType))
                    .doOnNext(next -> {
                        final String format = "Created consumer for Service Bus resource: [{}] mode: [{}]"
                            + " sessionEnabled? {} transferEntityPath: [{}], entityType: [{}]";
                        logger.verbose(format, next.getEntityPath(), receiveMode, isSessionEnabled, "N/A",
                            entityType);
                    })
                    .repeat();

            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionProcessor.getRetryOptions());
            final ServiceBusReceiveLinkProcessor linkMessageProcessor = receiveLink.subscribeWith(
                new ServiceBusReceiveLinkProcessor(prefetch, retryPolicy, connectionProcessor));

            return new ServiceBusAsyncConsumer(linkMessageProcessor, messageSerializer, isAutoComplete,
                connectionProcessor.getRetryOptions(), this::complete, this::abandon);
        });
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

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(getFullyQualifiedNamespace(), getServiceBusResourceName());
    }
}
