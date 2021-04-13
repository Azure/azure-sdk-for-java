// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.exception.AmqpErrorCondition;
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
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_RECEIVER;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * Package-private class that manages session aware message receiving.
 */
class ServiceBusSessionManager implements AutoCloseable {
    // Time to delay before trying to accept another session.
    private static final Duration SLEEP_DURATION_ON_ACCEPT_SESSION_EXCEPTION = Duration.ofMinutes(1);

    private final ClientLogger logger = new ClientLogger(ServiceBusSessionManager.class);
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusReceiveLink receiveLink;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final Duration operationTimeout;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicBoolean isStarted = new AtomicBoolean();
    private final List<Scheduler> schedulers;
    private final Deque<Scheduler> availableSchedulers = new ConcurrentLinkedDeque<>();
    private final Duration maxSessionLockRenewDuration;

    /**
     * SessionId to receiver mapping.
     */
    private final ConcurrentHashMap<String, ServiceBusSessionReceiver> sessionReceivers = new ConcurrentHashMap<>();
    private final EmitterProcessor<Flux<ServiceBusMessageContext>> processor;
    private final FluxSink<Flux<ServiceBusMessageContext>> sessionReceiveSink;

    private volatile Flux<ServiceBusMessageContext> receiveFlux;

    ServiceBusSessionManager(String entityPath, MessagingEntityType entityType,
        ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, ReceiverOptions receiverOptions, ServiceBusReceiveLink receiveLink) {
        this.entityPath = entityPath;
        this.entityType = entityType;
        this.receiverOptions = receiverOptions;
        this.connectionProcessor = connectionProcessor;
        this.operationTimeout = connectionProcessor.getRetryOptions().getTryTimeout();
        this.tracerProvider = tracerProvider;
        this.messageSerializer = messageSerializer;
        this.maxSessionLockRenewDuration = receiverOptions.getMaxLockRenewDuration();

        // According to the documentation, if a sequence is not finite, it should be published on their own scheduler.
        // It's possible that some of these sessions have a lot of messages.
        final int numberOfSchedulers = receiverOptions.isRollingSessionReceiver()
            ? receiverOptions.getMaxConcurrentSessions()
            : 1;

        final List<Scheduler> schedulerList = IntStream.range(0, numberOfSchedulers)
            .mapToObj(index -> Schedulers.newBoundedElastic(DEFAULT_BOUNDED_ELASTIC_SIZE,
                DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "receiver-" + index))
            .collect(Collectors.toList());

        this.schedulers = Collections.unmodifiableList(schedulerList);
        this.availableSchedulers.addAll(this.schedulers);

        this.processor = EmitterProcessor.create(numberOfSchedulers, false);
        this.sessionReceiveSink = processor.sink();
        this.receiveLink = receiveLink;
    }

    ServiceBusSessionManager(String entityPath, MessagingEntityType entityType,
        ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider,
        MessageSerializer messageSerializer, ReceiverOptions receiverOptions) {
        this(entityPath, entityType, connectionProcessor, tracerProvider,
            messageSerializer, receiverOptions, null);
    }

    /**
     * Gets the link name with the matching {@code sessionId}.
     *
     * @param sessionId Session id to get link name for.
     *
     * @return The name of the link, or {@code null} if there is no open link with that {@code sessionId}.
     */
    String getLinkName(String sessionId) {
        final ServiceBusSessionReceiver receiver = sessionReceivers.get(sessionId);
        return receiver != null ? receiver.getLinkName() : null;
    }

    /**
     * Gets the state of a session given its identifier.
     *
     * @param sessionId Identifier of session to get.
     *
     * @return The session state or an empty Mono if there is no state set for the session.
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    Mono<byte[]> getSessionState(String sessionId) {
        return validateParameter(sessionId, "sessionId", "getSessionState").then(
            getManagementNode().flatMap(channel -> {
                final ServiceBusSessionReceiver receiver = sessionReceivers.get(sessionId);
                final String associatedLinkName = receiver != null ? receiver.getLinkName() : null;

                return channel.getSessionState(sessionId, associatedLinkName);
            }));
    }

    /**
     * Gets a stream of messages from different sessions.
     *
     * @return A Flux of messages merged from different sessions.
     */
    Flux<ServiceBusMessageContext> receive() {
        if (!isStarted.getAndSet(true)) {
            this.sessionReceiveSink.onRequest(this::onSessionRequest);

            if (!receiverOptions.isRollingSessionReceiver()) {
                receiveFlux = getSession(schedulers.get(0), false);
            } else {
                receiveFlux = Flux.merge(processor, receiverOptions.getMaxConcurrentSessions());
            }
        }

        return receiveFlux;
    }

    /**
     * Renews the session lock.
     *
     * @param sessionId Identifier of session to get.
     *
     * @return The next expiration time for the session lock.
     * @throws IllegalStateException if the receiver is a non-session receiver.
     */
    Mono<OffsetDateTime> renewSessionLock(String sessionId) {
        return validateParameter(sessionId, "sessionId", "renewSessionLock").then(
            getManagementNode().flatMap(channel -> {
                final ServiceBusSessionReceiver receiver = sessionReceivers.get(sessionId);
                final String associatedLinkName = receiver != null ? receiver.getLinkName() : null;

                return channel.renewSessionLock(sessionId, associatedLinkName).handle((offsetDateTime, sink) -> {
                    if (receiver != null) {
                        receiver.setSessionLockedUntil(offsetDateTime);
                    }
                    sink.next(offsetDateTime);
                });
            }));
    }

    /**
     * Tries to update the message disposition on a session aware receive link.
     *
     * @return {@code true} if the {@code lockToken} was updated on receive link. {@code false} otherwise. This means
     *     there isn't an open link with that {@code sessionId}.
     */
    Mono<Boolean> updateDisposition(String lockToken, String sessionId,
        DispositionStatus dispositionStatus, Map<String, Object> propertiesToModify, String deadLetterReason,
        String deadLetterDescription, ServiceBusTransactionContext transactionContext) {

        final String operation = "updateDisposition";
        return Mono.when(
            validateParameter(lockToken, "lockToken", operation),
            validateParameter(lockToken, "lockToken", operation),
            validateParameter(sessionId, "'sessionId'", operation)).then(
            Mono.defer(() -> {
                final ServiceBusSessionReceiver receiver = sessionReceivers.get(sessionId);
                if (receiver == null || !receiver.containsLockToken(lockToken)) {
                    return Mono.just(false);
                }

                final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
                    deadLetterDescription, propertiesToModify, transactionContext);

                return receiver.updateDisposition(lockToken, deliveryState).thenReturn(true);
            }));
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        final List<Mono<Void>> closeables = sessionReceivers.values().stream()
            .map(receiver -> receiver.closeAsync())
            .collect(Collectors.toList());

        Mono.when(closeables).block(operationTimeout);
        sessionReceiveSink.complete();

        for (Scheduler scheduler : schedulers) {
            scheduler.dispose();
        }
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(connectionProcessor.getFullyQualifiedNamespace(), entityPath);
    }

    /**
     * Creates an session receive link.
     *
     * @return A Mono that completes with an session receive link.
     */
    private Mono<ServiceBusReceiveLink> createSessionReceiveLink() {
        final String sessionId = receiverOptions.getSessionId();

        final String linkName = (sessionId != null)
            ? sessionId
            : StringUtil.getRandomString("session-");
        return connectionProcessor
            .flatMap(connection -> {
                return connection.createReceiveLink(linkName, entityPath, receiverOptions.getReceiveMode(),
                null, entityType, sessionId);
            });
    }

    /**
     * Gets an active unnamed session link.
     *
     * @return A Mono that completes when an unnamed session becomes available.
     * @throws AmqpException if the session manager is already disposed.
     */
    Mono<ServiceBusReceiveLink> getActiveLink() {
        if (this.receiveLink != null) {
            return Mono.just(this.receiveLink);
        }
        return Mono.defer(() -> createSessionReceiveLink()
            .flatMap(link -> link.getEndpointStates()
                .takeUntil(e -> e == AmqpEndpointState.ACTIVE)
                .timeout(operationTimeout)
                .then(Mono.just(link))))
            .retryWhen(Retry.from(retrySignals -> retrySignals.flatMap(signal -> {
                final Throwable failure = signal.failure();
                logger.info("entityPath[{}] attempt[{}]. Error occurred while getting unnamed session.",
                    entityPath, signal.totalRetriesInARow(), failure);

                if (isDisposed.get()) {
                    return Mono.<Long>error(new AmqpException(false, "SessionManager is already disposed.", failure,
                        getErrorContext()));
                } else if (failure instanceof TimeoutException) {
                    return Mono.delay(SLEEP_DURATION_ON_ACCEPT_SESSION_EXCEPTION);
                } else if (failure instanceof AmqpException
                    && ((AmqpException) failure).getErrorCondition() == AmqpErrorCondition.TIMEOUT_ERROR) {
                    return Mono.delay(SLEEP_DURATION_ON_ACCEPT_SESSION_EXCEPTION);
                } else {
                    return Mono.<Long>error(failure);
                }
            })));
    }

    /**
     * Gets the next available unnamed session with the given receive options and publishes its contents on the given
     * {@code scheduler}.
     *
     * @param scheduler Scheduler to coordinate received methods on.
     * @param disposeOnIdle true to dispose receiver when it idles; false otherwise.
     * @return A Mono that completes with an unnamed session receiver.
     */
    private Flux<ServiceBusMessageContext> getSession(Scheduler scheduler, boolean disposeOnIdle) {
        return getActiveLink().flatMap(link -> link.getSessionId()
            .map(sessionId -> sessionReceivers.compute(sessionId, (key, existing) -> {
                if (existing != null) {
                    return existing;
                }

                return new ServiceBusSessionReceiver(link, messageSerializer, connectionProcessor.getRetryOptions(),
                    receiverOptions.getPrefetchCount(), disposeOnIdle, scheduler, this::renewSessionLock,
                    maxSessionLockRenewDuration);
            })))
            .flatMapMany(sessionReceiver -> sessionReceiver.receive().doFinally(signalType -> {
                logger.verbose("Closing session receiver for session id [{}].", sessionReceiver.getSessionId());
                availableSchedulers.push(scheduler);
                sessionReceivers.remove(sessionReceiver.getSessionId());
                sessionReceiver.closeAsync().subscribe();

                if (receiverOptions.isRollingSessionReceiver()) {
                    onSessionRequest(1L);
                }
            }))
            .publishOn(scheduler, 1);
    }

    private Mono<ServiceBusManagementNode> getManagementNode() {
        return connectionProcessor.flatMap(connection -> connection.getManagementNode(entityPath, entityType));
    }

    /**
     * Emits a new unnamed active session when it becomes available.
     *
     * @param request Number of unnamed active sessions to emit.
     */
    private void onSessionRequest(long request) {
        if (isDisposed.get()) {
            logger.info("Session manager is disposed. Not emitting more unnamed sessions.");
            return;
        }
        logger.verbose("Requested {} unnamed sessions.", request);
        for (int i = 0; i < request; i++) {
            final Scheduler scheduler = availableSchedulers.poll();

            // if there was no available scheduler and the number of requested items wasn't infinite. We were
            // expecting a free item. return an error.
            if (scheduler == null) {
                if (request != Long.MAX_VALUE) {
                    logger.verbose("request[{}]: There are no available schedulers to fetch.", request);
                }

                return;
            }

            Flux<ServiceBusMessageContext> session = getSession(scheduler, true);

            sessionReceiveSink.next(session);
        }
    }

    private <T> Mono<Void> validateParameter(T parameter, String parameterName, String operation) {
        if (isDisposed.get()) {
            return monoError(logger, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, operation)));
        } else if (parameter == null) {
            return monoError(logger, new NullPointerException(String.format("'%s' cannot be null.", parameterName)));
        } else if ((parameter instanceof String) && (((String) parameter).isEmpty())) {
            return monoError(logger, new IllegalArgumentException(String.format("'%s' cannot be an empty string.",
                parameterName)));
        } else {
            return Mono.empty();
        }
    }
}
