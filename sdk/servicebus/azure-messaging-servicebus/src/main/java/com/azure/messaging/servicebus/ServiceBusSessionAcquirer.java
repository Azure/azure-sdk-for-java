// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.ReactorConnectionCache;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink.SessionProperties;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;

final class ServiceBusSessionAcquirer implements AutoCloseable {
    private static final String TRACKING_ID_KEY = "trackingId";
    private final ClientLogger logger;
    private final String identifier;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final Duration sessionActiveTimeout;
    private final ServiceBusReceiveMode receiveMode;
    private final ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache;
    private final AtomicBoolean isDisposed = new AtomicBoolean();

    ServiceBusSessionAcquirer(ClientLogger logger, String identifier, String entityPath,
        MessagingEntityType entityType, ServiceBusReceiveMode receiveMode, Duration sessionActiveTimeout,
        ReactorConnectionCache<ServiceBusReactorAmqpConnection> connectionCache) {
        this.logger = logger;
        this.identifier = identifier;
        this.entityPath = entityPath;
        this.entityType = entityType;
        this.sessionActiveTimeout = sessionActiveTimeout;
        this.receiveMode = receiveMode;
        this.connectionCache = connectionCache;
    }

    /**
     * acquire a link to receive from the next available session.
     *
     * @return A Mono that completes when a session becomes available.
     * @throws AmqpException if the acquirer is already disposed.
     */
    Mono<Session> acquire() {
        return acquireIntern(null);
    }

    /**
     * acquire a link to receive from a session identified by the given {@code sessionId}.
     *
     * @return A Mono that completes when a session becomes available.
     * @throws NullPointerException if {@code sessionId} argument is null.
     * @throws AmqpException if the acquirer is already disposed.
     */
    Mono<Session> acquire(String sessionId) {
        Objects.requireNonNull(sessionId, "sessionId cannot be null.");
        return acquireIntern(sessionId);
    }

    private Mono<Session> acquireIntern(String sessionId) {
        return Mono.defer(() -> createSessionReceiveLink(sessionId)
            .flatMap(sessionLink -> sessionLink.getSessionProperties() // Await for sessionLink to "ACTIVE" then reads its properties
                .flatMap(sessionProperties -> {
                    return Mono.just(new Session(sessionLink, sessionProperties));
                })
            ))
            .timeout(sessionActiveTimeout)
            .retryWhen(Retry.from(retrySignals -> retrySignals.flatMap(signal -> {
                final Throwable failure = signal.failure();
                logger.atInfo()
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue("attempt", signal.totalRetriesInARow())
                    .log(sessionId == null
                        ? "Error occurred while getting unnamed session."
                        : "Error occurred while getting session " + sessionId,
                        failure);

                if (isDisposed.get()) {
                    return Mono.<Long>error(new AmqpException(false, "ServiceBusSessionLinkAcquirer is already disposed.",
                        failure, new SessionErrorContext(connectionCache.getFullyQualifiedNamespace(), entityPath)));
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
                    logger.atInfo()
                        .addKeyValue(TRACKING_ID_KEY, id)
                        .log("Unable to acquire new session.", failure);
                    // The link-endpoint-state publisher will emit signal on the reactor-executor thread, which is
                    // non-blocking, if we use the session processor to recover the error, it requires a blocking
                    // thread to close the client. Hence, we publish the error on the bounded-elastic thread.
                    return Mono.<Long>error(failure)
                        .publishOn(Schedulers.boundedElastic())
                        .doOnError(e -> logger.atInfo()
                            .addKeyValue(TRACKING_ID_KEY, id)
                            .log("Emitting the error signal received for session acquire attempt.", e)
                        );
                }
            })));
    }

    private Mono<ServiceBusReceiveLink> createSessionReceiveLink(String sessionId) {
        final String linkName = (sessionId != null) ? sessionId : StringUtil.getRandomString("session-");
        return connectionCache.get()
            .flatMap(connection -> connection.createReceiveLink(linkName, entityPath, receiveMode,
                null, entityType, identifier, sessionId));
    }

    @Override
    public void close() {
        isDisposed.getAndSet(true);
    }

    /**
     * A tuple type to hold properties and AmqpLink to a session.
     */
    static final class Session {
        final ServiceBusReceiveLink link;
        final SessionProperties properties;

        /**
         * Create Session tuple to hold properties and AmqpLink to a session.
         *
         * @param sessionLink the amqp link to the session.
         * @param sessionProperties the session properties.
         */
        Session(ServiceBusReceiveLink sessionLink, SessionProperties sessionProperties) {
            this.link = sessionLink;
            this.properties = sessionProperties;
        }

        boolean isDisposed() {
            return link.isDisposed();
        }

        Mono<Void> closeAsync() {
            return link.closeAsync();
        }
    }
}
