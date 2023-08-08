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
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
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

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_RECEIVER;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.NUMBER_OF_REQUESTED_MESSAGES_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * Package-private class that manages session aware message receiving.
 */
class ServiceBusSessionManager implements AutoCloseable {
    // Time to delay before trying to accept another session.
    private static final String TRACKING_ID_KEY = "trackingId";

    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSessionManager.class);
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusReceiveLink receiveLink;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final Duration operationTimeout;
    private final MessageSerializer messageSerializer;
    private final String identifier;

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final AtomicBoolean isStarted = new AtomicBoolean();
    private final List<Scheduler> schedulers;
    private final Deque<Scheduler> availableSchedulers = new ConcurrentLinkedDeque<>();
    private final Duration maxSessionLockRenewDuration;
    private final Duration sessionIdleTimeout;

    /**
     * SessionId to receiver mapping.
     */
    private final ConcurrentHashMap<String, ServiceBusSessionReceiver> sessionReceivers = new ConcurrentHashMap<>();
    private final EmitterProcessor<Flux<ServiceBusMessageContext>> processor;
    private final FluxSink<Flux<ServiceBusMessageContext>> sessionReceiveSink;
    private final ServiceBusTracer tracer;

    private volatile Flux<ServiceBusMessageContext> receiveFlux;

    ServiceBusSessionManager(String entityPath, MessagingEntityType entityType,
        ServiceBusConnectionProcessor connectionProcessor,
        MessageSerializer messageSerializer, ReceiverOptions receiverOptions, ServiceBusReceiveLink receiveLink, String identifier,
        ServiceBusTracer tracer) {
        this.entityPath = entityPath;
        this.entityType = entityType;
        this.receiverOptions = receiverOptions;
        this.connectionProcessor = connectionProcessor;
        this.operationTimeout = connectionProcessor.getRetryOptions().getTryTimeout();
        this.messageSerializer = messageSerializer;
        this.maxSessionLockRenewDuration = receiverOptions.getMaxLockRenewDuration();
        this.identifier = identifier;
        this.tracer = tracer;

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
        this.sessionIdleTimeout = receiverOptions.getSessionIdleTimeout() != null
            ? receiverOptions.getSessionIdleTimeout()
            : connectionProcessor.getRetryOptions().getTryTimeout();
    }

    ServiceBusSessionManager(String entityPath, MessagingEntityType entityType,
        ServiceBusConnectionProcessor connectionProcessor,
        MessageSerializer messageSerializer, ReceiverOptions receiverOptions, String identifier, ServiceBusTracer tracer) {
        this(entityPath, entityType, connectionProcessor,
            messageSerializer, receiverOptions, null, identifier, tracer);
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
     * Gets the identifier of the instance of {@link ServiceBusSessionManager}.
     *
     * @return The identifier that can identify the instance of {@link ServiceBusSessionManager}.
     */
    public String getIdentifier() {
        return this.identifier;
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
    private Mono<OffsetDateTime> renewSessionLock(String sessionId) {
        return validateParameter(sessionId, "sessionId", "renewSessionLock").then(
            getManagementNode().flatMap(channel -> {
                final ServiceBusSessionReceiver receiver = sessionReceivers.get(sessionId);
                final String associatedLinkName = receiver != null ? receiver.getLinkName() : null;

                return tracer.traceMono("ServiceBus.renewSessionLock", channel.renewSessionLock(sessionId, associatedLinkName))
                    .handle((offsetDateTime, sink) -> {
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
                null, entityType, identifier, sessionId);
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
                .filter(e -> e == AmqpEndpointState.ACTIVE)
                .next()
                // The reason for using 'switchIfEmpty' operator -
                //
                // While waiting for the link to ACTIVE, if the broker detaches the link without an error-condition,
                // the link-endpoint-state publisher will transition to completion without ever emitting ACTIVE. Map
                // such publisher completion to transient (i.e., retriable) AmqpException to enable retry.
                //
                // A detach without an error-condition can happen when Service upgrades. Also, while the service often
                // detaches with the error-condition 'com.microsoft:timeout' when there is no session, sometimes,
                // when a free or new session is unavailable, detach can happen without the error-condition.
                //
                .switchIfEmpty(Mono.error(() ->
                    new AmqpException(true, "Session receive link completed without being active", null)))
                .timeout(operationTimeout)
                .then(Mono.just(link))))
            .retryWhen(Retry.from(retrySignals -> retrySignals.flatMap(signal -> {
                final Throwable failure = signal.failure();
                LOGGER.atInfo()
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue("attempt", signal.totalRetriesInARow())
                    .log("Error occurred while getting unnamed session.", failure);

                if (isDisposed.get()) {
                    return Mono.<Long>error(new AmqpException(false, "SessionManager is already disposed.", failure,
                        getErrorContext()));
                } else if (failure instanceof TimeoutException) {
                    return Mono.delay(Duration.ZERO);
                } else if (failure instanceof AmqpException
                    && ((AmqpException) failure).getErrorCondition() == AmqpErrorCondition.TIMEOUT_ERROR) {
                    // The link closed remotely with 'Detach {errorCondition:com.microsoft:timeout}' frame because
                    // the broker waited for N seconds (60 sec hard limit today) but there was no free or new session.
                    //
                    // Given N seconds elapsed since the last session acquire attempt, request for a session on
                    // the 'parallel' Scheduler and free the 'QPid' thread for other IO.
                    //
                    return Mono.delay(Duration.ZERO);
                } else {
                    final long id = System.nanoTime();
                    LOGGER.atInfo()
                            .addKeyValue(TRACKING_ID_KEY, id)
                            .log("Unable to acquire new session.", failure);
                    // The link-endpoint-state publisher will emit signal on the reactor-executor thread, which is
                    // non-blocking, if we use the session processor to recover the error, it requires a blocking
                    // thread to close the client. Hence, we publish the error on the bounded-elastic thread.
                    return Mono.<Long>error(failure)
                            .publishOn(Schedulers.boundedElastic())
                            .doOnError(e -> LOGGER.atInfo()
                                    .addKeyValue(TRACKING_ID_KEY, id)
                                    .log("Emitting the error signal received for session acquire attempt.", e)
                    );
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

                final Duration idleTimeout = disposeOnIdle ? sessionIdleTimeout : null;
                return new ServiceBusSessionReceiver(sessionId, link, messageSerializer, connectionProcessor.getRetryOptions(),
                    receiverOptions.getPrefetchCount(), scheduler, this::renewSessionLock,
                    maxSessionLockRenewDuration, idleTimeout);
            })))
            .flatMapMany(sessionReceiver -> sessionReceiver.receive().doFinally(signalType -> {
                LOGGER.atVerbose()
                    .addKeyValue(SESSION_ID_KEY, sessionReceiver.getSessionId())
                    .log("Closing session receiver.");

                availableSchedulers.push(scheduler);
                sessionReceivers.remove(sessionReceiver.getSessionId());
                sessionReceiver.closeAsync().subscribe();

                if (receiverOptions.isRollingSessionReceiver()) {
                    onSessionRequest(1L);
                }
            }));
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
            LOGGER.info("Session manager is disposed. Not emitting more unnamed sessions.");
            return;
        }

        LOGGER.atVerbose()
            .addKeyValue(NUMBER_OF_REQUESTED_MESSAGES_KEY, request)
            .log("Requested unnamed sessions.");

        for (int i = 0; i < request; i++) {
            final Scheduler scheduler = availableSchedulers.poll();

            // if there was no available scheduler and the number of requested items wasn't infinite. We were
            // expecting a free item. return an error.
            if (scheduler == null) {
                if (request != Long.MAX_VALUE) {
                    LOGGER.atVerbose()
                        .addKeyValue(NUMBER_OF_REQUESTED_MESSAGES_KEY, request)
                        .log("There are no available schedulers to fetch.");
                }

                return;
            }

            Flux<ServiceBusMessageContext> session = getSession(scheduler, true);

            sessionReceiveSink.next(session);
        }
    }

    private <T> Mono<Void> validateParameter(T parameter, String parameterName, String operation) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RECEIVER, operation)));
        } else if (parameter == null) {
            return monoError(LOGGER, new NullPointerException(String.format("'%s' cannot be null.", parameterName)));
        } else if ((parameter instanceof String) && (((String) parameter).isEmpty())) {
            return monoError(LOGGER, new IllegalArgumentException(String.format("'%s' cannot be an empty string.",
                parameterName)));
        } else {
            return Mono.empty();
        }
    }
}
