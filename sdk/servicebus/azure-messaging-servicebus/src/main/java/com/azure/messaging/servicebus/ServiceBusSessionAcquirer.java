// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpClientOptions;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
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

    /**
     * Tries to acquire a session from the broker by opening an AMQP receive link. When the acquire attempt timeout then
     * the api will retry if {@code timeoutRetryDisabled} is set to {@code false}. In case an error needs to be propagated,
     * api publishes the error using bounded-elastic Thread.
     *
     * @param sessionId the unique id of the specific session to acquire, a value {@code null} means acquire any free session.
     * @return A Mono that completes with the acquired session, the Mono can emit {@link AmqpException} if the acquirer
     * is already disposed or {@link TimeoutException} if session acquire timeouts and {@code timeoutRetryDisabled} set to true.
     */
    private Mono<Session> acquireIntern(String sessionId) {
        if (timeoutRetryDisabled) {
            return acquireSession(sessionId).onErrorResume(t -> {
                if (isBrokerTimeoutError(t)) {
                    // map the broker timeout to application-friendly TimeoutException.
                    final Throwable e = new TimeoutException("com.microsoft:timeout").initCause(t);
                    return publishError(sessionId, e, false);
                }
                return publishError(sessionId, t, true);
            });
        } else {
            return acquireSession(sessionId).timeout(tryTimeout)
                .retryWhen(Retry.from(signals -> signals.flatMap(signal -> {
                    final Throwable t = signal.failure();
                    if (isTimeoutError(t)) {
                        logger.atVerbose()
                            .addKeyValue(ENTITY_PATH_KEY, entityPath)
                            .addKeyValue("attempt", signal.totalRetriesInARow())
                            .log("Timeout while acquiring session '{}'.", sessionName(sessionId), t);
                        // retry session acquire using Schedulers.parallel() and free the QPid thread.
                        return Mono.delay(Duration.ZERO);
                    }
                    return publishError(sessionId, t, true);
                })));
        }
    }

    /**
     * Tries to acquire a session from the broker by opening an AMQP receive link.
     *
     * @param sessionId the unique id of the session to acquire, a value {@code null} means acquire any free session.
     *
     * @return the acquired session.
     */
    private Mono<Session> acquireSession(String sessionId) {
        return Mono.defer(() -> {
            final Mono<ServiceBusReceiveLink> createLink = connectionCacheWrapper.getConnection()
                .flatMap(connection -> connection.createReceiveLink(linkName(sessionId), entityPath, receiveMode, null,
                    entityType, identifier, sessionId));
            return createLink.flatMap(link -> {
                // ServiceBusReceiveLink::getSessionProperties() await for link to "ACTIVE" then reads its properties.
                return link.getSessionProperties()
                    .flatMap(sessionProperties -> Mono.just(new Session(link, sessionProperties, sessionManagement)));
            });
        });
    }

    /**
     * Publish the session acquire error using a bounded-elastic Thread.
     * <p>
     * The link-endpoint-state publisher ({@link ReceiveLinkHandler2#getEndpointStates()}) will emit error on the QPid
     * Thread, which is a non-block-able Thread. Publishing the error on the (block-able) bounded-elastic Thread will free
     * QPid Thread and to allow any blocking operation that downstream may do. If library do not publish in bounded-elastic
     * Thread and downstream happens to make a blocking call on non-block-able QPid Thread then reactor-core will error
     * - 'IllegalStateException(..*operation* are blocking, which is not supported in thread ...').
     * </p>
     *
     * @param sessionId the session id.
     * @param t the error to publish.
     * @param logAtInfo indicates if session acquire error should be logged at "info" level from the current thread, most of
     *     the time, the broker timeout is the reason for session acquisition failure, in case, the acquire fails
     *     due to any other reasons, that least expected error is logged in the "info" level.
     * @return a Mono that publishes the given error using a bounded-elastic Thread.
     * @param <T> the type
     */
    private <T> Mono<T> publishError(String sessionId, Throwable t, boolean logAtInfo) {
        final long id = System.nanoTime();
        if (logAtInfo) {
            logger.atInfo()
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(TRACKING_ID_KEY, id)
                .log("Unable to acquire session '{}'.", sessionName(sessionId), t);
        }
        return Mono.<T>error(t)
            .publishOn(Schedulers.boundedElastic())
            .doOnError(ignored -> logger.atVerbose()
                .addKeyValue(TRACKING_ID_KEY, id)
                .log("Emitting session acquire error" + (logAtInfo ? "." : ": " + t.getMessage())));
    }

    /**
     * Check if the given error is a remote link detach with '{errorCondition:com.microsoft:timeout}' indicating the broker
     * waited for N seconds (60 sec default) but there was no free or new session.
     *
     * @param t the error to test.
     * @return {@code true} if the error represents broker timeout.
     */
    private static boolean isBrokerTimeoutError(Throwable t) {
        return t instanceof AmqpException
            && ((AmqpException) t).getErrorCondition() == AmqpErrorCondition.TIMEOUT_ERROR;
    }

    /**
     * Checks if the given error is a timeout error.
     *
     * @param t the error to test.
     * @return {@code true} if the error represents timeout.
     */
    private static boolean isTimeoutError(Throwable t) {
        return t instanceof TimeoutException || isBrokerTimeoutError(t);
    }

    /**
     * Obtain the name for the AMQP link that channels messages from a session.
     *
     * @param sessionId the session to channel messages from.
     * @return name for the AMQP link.
     */
    private static String linkName(String sessionId) {
        return (sessionId != null) ? sessionId : StringUtil.getRandomString("session-");
    }

    /**
     * Get the session name for simple local logging purpose.
     *
     * @param sessionId the unique id of the session or {@code null}, if session id is unknown.
     * @return the session name.
     */
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
