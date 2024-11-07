// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpClientOptions;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink.SessionProperties;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;

/**
 * A type to acquire a session from a session enabled Service Bus entity. If the broker cannot find a session within
 * the timeout, it returns a timeout error. The acquirer retries on timeout unless disabled via {@code timeoutRetryDisabled}.
 * <p>
 * The {@code timeoutRetryDisabled} is true when the session acquirer is used for synchronous {@link ServiceBusSessionReceiverClient}.
 * This allows the synchronous 'acceptNextSession()' API to propagate the broker timeout error if no session is available.
 * The 'acceptNextSession()' has a client-side timeout that is set slightly longer than the broker's timeout, ensuring
 * the broker's timeout usually triggers first (the client-side timeout still helps in case of unexpected hanging).
 * For ServiceBusSessionReceiverClient, if the library retries session-acquire on broker timeout, the client-side sync
 * timeout might expire while waiting. When client-side timeout expires like this, library cannot cancel the outstanding
 * acquire request to the broker, which means, the broker may still lock a session for an acquire request that nobody
 * is waiting on, resulting that session to be unavailable for any other 'acceptNextSession()' until initial broker
 * lock expires. Hence, library propagate the broker timeout error in ServiceBusSessionReceiverClient case.
 * </p>
 * <p>
 * For session enabled {@link ServiceBusProcessorClient} and {@link ServiceBusSessionReceiverAsyncClient},
 * the {@code timeoutRetryDisabled} is false, hence session acquirer retries on broker timeout.
 * </p>
 */
final class ServiceBusSessionAcquirer {
    private static final String TRACKING_ID_KEY = "trackingId";
    private final ClientLogger logger;
    private final String identifier;
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final Duration tryTimeout;
    private final boolean timeoutRetryDisabled;
    private final ServiceBusReceiveMode receiveMode;
    private final ConnectionCacheWrapper connectionCacheWrapper;
    private final Mono<ServiceBusManagementNode> sessionManagement;

    /**
     * Creates ServiceBusSessionAcquirer to acquire session from a session enabled entity.
     *
     * @param logger the logger to use.
     * @param identifier the client identifier, currently callsites uses {@link AmqpClientOptions#getIdentifier()}.
     * @param entityPath path to the session enabled entity.
     * @param entityType the entity type (e.g., queue, topic subscription)
     * @param receiveMode the mode of receiving messages from the acquired session.
     * @param tryTimeout the try timeout, currently callsites uses {@link AmqpRetryOptions#getTryTimeout()}}.
     * @param timeoutRetryDisabled if session acquire retry should be disabled when broker timeout on no session.
     * @param connectionCacheWrapper the connection cache.
     */
    ServiceBusSessionAcquirer(ClientLogger logger, String identifier, String entityPath, MessagingEntityType entityType,
        ServiceBusReceiveMode receiveMode, Duration tryTimeout, boolean timeoutRetryDisabled,
        ConnectionCacheWrapper connectionCacheWrapper) {
        assert connectionCacheWrapper.isV2();
        this.logger = logger;
        this.identifier = identifier;
        this.entityPath = entityPath;
        this.entityType = entityType;
        this.tryTimeout = tryTimeout;
        this.timeoutRetryDisabled = timeoutRetryDisabled;
        this.receiveMode = receiveMode;
        this.connectionCacheWrapper = connectionCacheWrapper;
        this.sessionManagement = connectionCacheWrapper.getConnection()
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType));
    }

    boolean isConnectionClosed() {
        return connectionCacheWrapper.isChannelClosed();
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
        final Mono<Session> acquireSession
            = Mono.defer(() -> createSessionReceiveLink(sessionId).flatMap(link -> link.getSessionProperties() // Await for link to "ACTIVE" then reads its properties
                .flatMap(sessionProperties -> Mono.just(new Session(link, sessionProperties, sessionManagement)))));

        return (timeoutRetryDisabled ? acquireSession : acquireSession.timeout(tryTimeout))
            .retryWhen(Retry.from(retrySignals -> retrySignals.flatMap(signal -> {
                final Throwable failure = signal.failure();
                final boolean isTimeoutError = isTimeoutError(failure);
                if (isTimeoutError && !timeoutRetryDisabled) {
                    logger.atVerbose()
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .addKeyValue("attempt", signal.totalRetriesInARow())
                        .log("Broker timeout while acquiring session '{}'.", sessionName(sessionId), failure);
                    // retry for a session on Schedulers.parallel() and free the QPid thread for other IO.
                    return Mono.delay(Duration.ZERO);
                }
                final long id = System.nanoTime();
                if (!isTimeoutError) {
                    // Not a typical path as most of the time we expect broker timeout error, hence 'info' log.
                    logger.atInfo()
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .addKeyValue(TRACKING_ID_KEY, id)
                        .log("Unable to acquire session '{}'.", sessionName(sessionId), failure);
                }
                return propagateError(id, failure);
            })));
    }

    private Mono<ServiceBusReceiveLink> createSessionReceiveLink(String sessionId) {
        final String linkName = (sessionId != null) ? sessionId : StringUtil.getRandomString("session-");
        return connectionCacheWrapper.getConnection()
            .flatMap(connection -> connection.createReceiveLink(linkName, entityPath, receiveMode, null, entityType,
                identifier, sessionId));
    }

    private Mono<Long> propagateError(long id, Throwable failure) {
        final Throwable t;
        if (failure instanceof AmqpException
            && ((AmqpException) failure).getErrorCondition() == AmqpErrorCondition.TIMEOUT_ERROR) {
            // The broker timeout event needs to propagated to the application when timeoutRetryDisabled is true.
            // In such case, map the broker error (AmqpException with TIMEOUT_ERROR condition) to more user-friendly
            // TimeoutException.
            t = new TimeoutException().initCause(failure);
        } else {
            t = failure;
        }
        // The link-endpoint-state publisher will emit error on the QPid Thread, that is a non-blocking Thread,
        // publish the error on the (block-able) bounded-elastic thread to free QPid thread and to allow
        // any blocking operation that downstream may do.
        return Mono.<Long>error(t)
            .publishOn(Schedulers.boundedElastic())
            .doOnError(
                e -> logger.atVerbose().addKeyValue(TRACKING_ID_KEY, id).log("Emitting session acquire error signal."));
    }

    private static boolean isTimeoutError(Throwable failure) {
        if (failure instanceof TimeoutException) {
            return true;
        }
        if (failure instanceof AmqpException
            && ((AmqpException) failure).getErrorCondition() == AmqpErrorCondition.TIMEOUT_ERROR) {
            // The link closed remotely with 'Detach {errorCondition:com.microsoft:timeout}' frame because
            // the broker waited for N seconds (60 sec default) but there was no free or new session.
            return true;
        }
        return false;
    }

    private static String sessionName(String sessionId) {
        return sessionId == null ? "unnamed" : sessionId;
    }

    /**
     * A type to hold properties and AmqpLink to a session, and allow initiating recurring session lock renewal.
     */
    static final class Session {
        private final ServiceBusReceiveLink link;
        private final SessionProperties properties;
        private final Mono<ServiceBusManagementNode> sessionManagement;

        /**
         * Create Session instance representing .
         *
         * @param sessionLink the amqp link to the session.
         * @param sessionProperties the session properties.
         * @param sessionManagement Mono to get management node for session lock renewal.
         */
        Session(ServiceBusReceiveLink sessionLink, SessionProperties sessionProperties,
            Mono<ServiceBusManagementNode> sessionManagement) {
            this.link = Objects.requireNonNull(sessionLink, "sessionLink cannot be null.");
            this.properties = Objects.requireNonNull(sessionProperties, "sessionProperties cannot be null.");
            this.sessionManagement = Objects.requireNonNull(sessionManagement, "sessionManagement cannot be null.");
        }

        /**
         * Gets the session id.
         *
         * @return the session id.
         */
        String getId() {
            return properties.getId();
        }

        /**
         * Gets the link streaming messages from the session and session endpoint events.
         *
         * @return the session link.
         */
        ServiceBusReceiveLink getLink() {
            return link;
        }

        /**
         * Begin the recurring lock renewal for the session.
         *
         * @param tracer the tracer.
         * @param maxSessionLockRenew the upper bound for the recurring renewal duration.
         * @return {@link Disposable} that when disposed of, results in stopping the recurring renewal.
         */
        Disposable beginLockRenew(ServiceBusTracer tracer, Duration maxSessionLockRenew) {
            final String sessionId = properties.getId();
            // Function, when invoked, renews this session lock once.
            final Function<String, Mono<OffsetDateTime>> lockRenewFunc = __ -> {
                return sessionManagement.flatMap(mgmt -> {
                    final Mono<OffsetDateTime> renewLock = mgmt.renewSessionLock(sessionId, link.getLinkName());
                    return tracer.traceMono("ServiceBus.renewSessionLock", renewLock);
                });
            };

            final OffsetDateTime initialLockedUntil = properties.getLockedUntil();
            // The operation that recurs renewal (with an upper bound of maxSessionLockRenew) using the above 'lockRenewFunc'.
            final LockRenewalOperation recurringLockRenew
                = new LockRenewalOperation(sessionId, maxSessionLockRenew, true, lockRenewFunc, initialLockedUntil);
            return recurringLockRenew;
        }
    }
}
