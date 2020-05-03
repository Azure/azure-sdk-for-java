// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_RECEIVER;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * Package-private class that manages session aware message receiving.
 */
class UnnamedSessionManager implements AutoCloseable {
    // Time to delay before trying to accept another session.
    private static final Duration SLEEP_DURATION_ON_ACCEPT_SESSION_EXCEPTION = Duration.ofMinutes(1);

    private final ClientLogger logger = new ClientLogger(UnnamedSessionManager.class);
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final Duration operationTimeout;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicBoolean isStarted = new AtomicBoolean();
    private final List<Scheduler> schedulers;
    private final Map<String, Scheduler> sessionIdToScheduler = new HashMap<>();
    private final ConcurrentHashMap<String, UnnamedSessionReceiver> sessionReceivers = new ConcurrentHashMap<>();

    private final EmitterProcessor<Flux<ServiceBusReceivedMessageContext>> processor = EmitterProcessor.create();
    private final FluxSink<Flux<ServiceBusReceivedMessageContext>> sessionReceiveSink = processor.sink();
    private final Flux<ServiceBusReceivedMessageContext> receiveFlux = Flux.merge(processor);

    private volatile ReceiveAsyncOptions receiveAsyncOptions;

    UnnamedSessionManager(String entityPath, MessagingEntityType entityType, ServiceBusConnectionProcessor connectionProcessor,
        Duration operationTimeout, TracerProvider tracerProvider, MessageSerializer messageSerializer,
        ReceiverOptions receiverOptions) {

        this.entityPath = entityPath;
        this.entityType = entityType;
        this.receiverOptions = receiverOptions;
        this.connectionProcessor = connectionProcessor;
        this.operationTimeout = operationTimeout;
        this.tracerProvider = tracerProvider;
        this.messageSerializer = messageSerializer;

        // According to the documentation, if a sequence is not finite, it should be published on their own scheduler.
        // It's possible that some of these sessions have a lot of messages.
        final int numberSchedulers = receiverOptions.isRollingSessionReceiver()
            ? receiverOptions.getMaxConcurrentSessions()
            : 1;

        final List<Scheduler> schedulerList = IntStream.range(0, receiverOptions.getMaxConcurrentSessions())
            .mapToObj(index -> Schedulers.newBoundedElastic(DEFAULT_BOUNDED_ELASTIC_SIZE,
                DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "receiver-" + index))
            .collect(Collectors.toList());

        this.schedulers = Collections.unmodifiableList(schedulerList);
    }

    /**
     * Gets a stream of messages from different sessions.
     *
     * @return A Flux of messages merged from different sessions.
     */
    Flux<ServiceBusReceivedMessageContext> receive(ReceiveAsyncOptions options) {
        if (options == null) {
            return fluxError(logger, new NullPointerException("'options' cannot be null."));
        }

        if (!isStarted.getAndSet(true)) {
            receiveAsyncOptions = options;
        }

        return receiveFlux;
    }

    Mono<Boolean> abandon(MessageLockToken lockToken, String sessionId, Map<String, Object> propertiesToModify) {
        return updateDisposition(lockToken, sessionId, DispositionStatus.ABANDONED, propertiesToModify,
            null, null);
    }

    Mono<Boolean> complete(MessageLockToken lockToken, String sessionId) {
        return updateDisposition(lockToken, sessionId, DispositionStatus.COMPLETED, null,
            null, null);
    }

    Mono<Boolean> defer(MessageLockToken lockToken, Map<String, Object> propertiesToModify, String sessionId) {
        return updateDisposition(lockToken, sessionId, DispositionStatus.DEFERRED, propertiesToModify,
            null, null);
    }

    Mono<Boolean> deadLetter(MessageLockToken lockToken, String sessionId, DeadLetterOptions deadLetterOptions) {
        return updateDisposition(lockToken, sessionId, DispositionStatus.SUSPENDED,
            deadLetterOptions.getPropertiesToModify(), deadLetterOptions.getDeadLetterReason(),
            deadLetterOptions.getDeadLetterErrorDescription());
    }

    /**
     * Gets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     * @return The session state or an empty Mono if there is no state set for the session.
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    Mono<byte[]> getSessionState(String sessionId) {
        return validateParameter(sessionId, "sessionId").then(
            getManagementNode().flatMap(channel -> {
                final UnnamedSessionReceiver receiver = sessionReceivers.get(sessionId);
                final String associatedLinkName = receiver != null ? receiver.getLinkName() : null;

                return channel.getSessionState(sessionId, associatedLinkName);
            }));
    }

    Mono<ServiceBusReceivedMessage> peek(String sessionId) {
        final UnnamedSessionReceiver receiver = sessionReceivers.get(sessionId);
        final long sequenceNumber = receiver != null ? receiver.getLastPeekedSequenceNumber() : -1;

        return peekAt(sequenceNumber, sessionId);
    }

    Mono<ServiceBusReceivedMessage> peekAt(long sequenceNumber, String sessionId) {
        return validateParameter(sessionId, "sessionId")
            .then(connectionProcessor.flatMap(connection -> connection.getManagementNode(entityPath, entityType))
                .flatMap(channel -> {
                    final UnnamedSessionReceiver receiver = sessionReceivers.get(sessionId);
                    final String linkName = receiver != null ? receiver.getLinkName() : null;

                    return channel.peek(sequenceNumber, sessionId, linkName);
                }));
    }

    Flux<ServiceBusReceivedMessage> peekBatch(int maxMessages, String sessionId) {
        final UnnamedSessionReceiver receiver = sessionReceivers.get(sessionId);
        final long sequenceNumber = receiver != null ? receiver.getLastPeekedSequenceNumber() : -1;

        return peekBatchAt(maxMessages, sequenceNumber, sessionId);
    }

    Flux<ServiceBusReceivedMessage> peekBatchAt(int maxMessages, long sequenceNumber, String sessionId) {
        return validateParameter(sessionId, "sessionId").thenMany(
            getManagementNode().flatMapMany(channel -> {
                final UnnamedSessionReceiver receiver = sessionReceivers.get(sessionId);
                final String linkName = receiver != null ? receiver.getLinkName() : null;

                return channel.peek(sequenceNumber, sessionId, linkName, maxMessages)
                    .map(message -> {
                        receiver.setLastPeekedSequenceNumber(message.getSequenceNumber());
                        return message;
                    });
            }));
    }

    Mono<ServiceBusReceivedMessage> receiveDeferredMessage(long sequenceNumber, String sessionId) {
        return receiveDeferredMessage(Collections.singleton(sequenceNumber), sessionId).next();
    }

    Flux<ServiceBusReceivedMessage> receiveDeferredMessage(Iterable<Long> sequenceNumbers, String sessionId) {
        return Flux.empty();
    }

    /**
     * Sets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     * @return The next expiration time for the session lock.
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    Mono<Instant> renewSessionLock(String sessionId) {
        return validateParameter(sessionId, "sessionId").then(getManagementNode().flatMap(channel -> {
            final UnnamedSessionReceiver receiver = sessionReceivers.get(sessionId);
            final String associatedLinkName = receiver != null ? receiver.getLinkName() : null;

            return channel.renewSessionLock(sessionId, associatedLinkName).handle((instant, sink) -> {
                if (receiver != null) {
                    receiver.setSessionLockedUntil(instant);
                }

                sink.next(instant);
            });
        }));
    }

    /**
     * Sets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     * @param sessionState State to set on the session.
     * @return A Mono that completes when the session is set
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    Mono<Void> setSessionState(String sessionId, byte[] sessionState) {
        return validateParameter(sessionId, "sessionId").then(getManagementNode().flatMap(channel -> {
            final UnnamedSessionReceiver receiver = sessionReceivers.get(sessionId);
            final String associatedLinkName = receiver != null ? receiver.getLinkName() : null;

            return channel.setSessionState(sessionId, sessionState, associatedLinkName);
        }));
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        for (Scheduler scheduler : schedulers) {
            scheduler.dispose();
        }
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(connectionProcessor.getFullyQualifiedNamespace(), entityPath);
    }

    /**
     * Gets whether or not the link contains the message lock token and it has not expired. Lock tokens are held by the
     * receive link when an unsettled delivery exists on the link.
     *
     * @param lockToken Lock token to check for.
     * @return {@code true} a  contains the lock token and false otherwise.
     */
    private UnnamedSessionReceiver getMatchingReceiver(String lockToken) {
        Optional<UnnamedSessionReceiver> matching = sessionReceivers.entrySet()
            .parallelStream()
            .filter(entry -> {
                UnnamedSessionReceiver value = entry.getValue();
                return value != null && value.containsLockToken(lockToken);
            })
            .map(entry -> entry.getValue())
            .findFirst();

        return matching.orElse(null);
    }

    /**
     * Gets the next available unnamed session with the given receive options.
     *
     * @param options Receive options.
     * @return A Mono that completes with an unnamed session receiver.
     */
    private Mono<UnnamedSessionReceiver> getSession(ReceiveAsyncOptions options) {
        return getActiveLink()
            .map(link -> new UnnamedSessionReceiver(link, messageSerializer, options.isEnableAutoComplete(),
                false, null, connectionProcessor.getRetryOptions()));
    }

    private Flux<ServiceBusReceivedMessageContext> getSession(ReceiveAsyncOptions options, Scheduler scheduler) {
        return getSession(options).flatMapMany(session -> session.receive()).publishOn(scheduler);
    }

    /**
     * Gets an active unnamed session link.
     *
     * @return A Mono that completes when an unnamed session becomes available.
     * @throws AmqpException if the session manager is already disposed.
     */
    private Mono<ServiceBusReceiveLink> getActiveLink() {
        return Mono.defer(() -> {
            return createSessionReceiveLink()
                .flatMap(link -> link.getEndpointStates()
                    .takeUntil(e -> e == AmqpEndpointState.ACTIVE)
                    .timeout(operationTimeout)
                    .then(Mono.just(link)));
        })
            .retryWhen(Retry.from(retrySignals -> retrySignals.flatMap(signal -> {
                final Throwable failure = signal.failure();
                logger.info("entityPath[{}] attempt[{}]. Error occurred while getting unnamed session.",
                    entityPath, signal.totalRetriesInARow(), failure);

                if (isDisposed.get()) {
                    return Mono.<Long>error(new AmqpException(false, "SessionManager is already disposed.", failure,
                        getErrorContext()));
                } else if (failure instanceof TimeoutException) {
                    return Mono.delay(Duration.ofSeconds(4));
                } else {
                    return Mono.<Long>error(failure);
                }
            })));
    }

    /**
     * Creates an unnamed session receive link.
     *
     * @return A Mono that completes with an unnamed session receive link.
     */
    private Mono<ServiceBusReceiveLink> createSessionReceiveLink() {
        final String linkName = StringUtil.getRandomString("session-");

        return connectionProcessor
            .flatMap(connection -> connection.createReceiveLink(linkName, entityPath, receiverOptions.getReceiveMode(),
                null, entityType, null));
    }

    private Mono<Boolean> updateDisposition(MessageLockToken lockToken, String sessionId,
        DispositionStatus dispositionStatus, Map<String, Object> propertiesToModify, String deadLetterReason,
        String deadLetterDescription) {

        if (lockToken == null) {
            return monoError(logger, new NullPointerException("'lockToken' cannot be null."));
        }

        return Mono.when(validateParameter(lockToken.getLockToken(), "lockToken.getLockToken()"),
            validateParameter(sessionId, "'sessionId'")).then(Mono.defer(() -> {

            final UnnamedSessionReceiver receiver = sessionReceivers.get(sessionId);
            if (receiver == null) {
                return Mono.just(false);
            }

            final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
                deadLetterDescription, propertiesToModify);

            return receiver.updateDisposition(lockToken.getLockToken(), deliveryState).thenReturn(true);
        }));
    }

    private Mono<ServiceBusManagementNode> getManagementNode() {
        return connectionProcessor.flatMap(connection -> connection.getManagementNode(entityPath, entityType));
    }

    private Mono<Void> validateParameter(String parameter, String parameterName) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, "getSessionState")));
        } else if (parameter == null) {
            return monoError(logger, new NullPointerException(String.format("'%s' cannot be null.", parameterName)));
        } else if (parameter.isEmpty()) {
            return monoError(logger, new IllegalArgumentException(String.format("'%s' cannot be an empty string.",
                parameterName)));
        } else {
            return Mono.empty();
        }
    }
}
