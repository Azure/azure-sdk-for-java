// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorReceiver;
import com.azure.core.amqp.implementation.ReceiversPumpingScheduler;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusReactorSession.LOCKED_UNTIL_UTC;
import static com.azure.messaging.servicebus.implementation.ServiceBusReactorSession.SESSION_FILTER;

/**
 * A proton-j receiver for Service Bus.
 */
public class ServiceBusReactorReceiver extends ReactorReceiver implements ServiceBusReceiveLink {
    private final ClientLogger logger;
    private final Mono<String> sessionIdMono;
    private final Mono<OffsetDateTime> sessionLockedUntil;
    private final Mono<SessionProperties> sessionProperties;

    public ServiceBusReactorReceiver(AmqpConnection connection, String entityPath, Receiver receiver,
        ReceiveLinkHandler2 handler, TokenManager tokenManager, ReactorDispatcher dispatcher,
        AmqpRetryOptions retryOptions) {
        super(connection, entityPath, receiver, handler, tokenManager, dispatcher, retryOptions,
            new AmqpMetricsProvider(null, connection.getFullyQualifiedNamespace(), entityPath));

        Map<String, Object> loggingContext = new HashMap<>(2);
        loggingContext.put(LINK_NAME_KEY, handler.getLinkName());
        loggingContext.put(ENTITY_PATH_KEY, entityPath);
        this.logger = new ClientLogger(ServiceBusReactorReceiver.class, loggingContext);

        this.sessionIdMono = getEndpointStates().filter(x -> x == AmqpEndpointState.ACTIVE).next().flatMap(state -> {
            @SuppressWarnings("unchecked")
            final Map<Symbol, Object> remoteSource = ((Source) receiver.getRemoteSource()).getFilter();
            final Object value = remoteSource.get(SESSION_FILTER);
            if (value == null) {
                logger.info("There is no session id.");
                return Mono.empty();
            }

            final String actualSessionId = String.valueOf(value);
            return Mono.just(actualSessionId);
        }).cache(value -> Duration.ofMillis(Long.MAX_VALUE), error -> Duration.ZERO, () -> Duration.ZERO);

        this.sessionLockedUntil = getEndpointStates().filter(x -> x == AmqpEndpointState.ACTIVE).next().map(state -> {
            if (receiver.getRemoteProperties() != null
                && receiver.getRemoteProperties().containsKey(LOCKED_UNTIL_UTC)) {
                final long ticks = (long) receiver.getRemoteProperties().get(LOCKED_UNTIL_UTC);
                return MessageUtils.convertDotNetTicksToOffsetDateTime(ticks);
            } else {
                logger.info("Locked until not set.");

                return Instant.EPOCH.atOffset(ZoneOffset.UTC);
            }
        }).cache(value -> Duration.ofMillis(Long.MAX_VALUE), error -> Duration.ZERO, () -> Duration.ZERO);

        this.sessionProperties = getEndpointStates().filter(x -> x == AmqpEndpointState.ACTIVE)
            .next()
            // While waiting for the link to ACTIVE, if the broker detaches the link without an error-condition,
            // the link-endpoint-state publisher will transition to completion without ever emitting ACTIVE. Map
            // such publisher completion to transient AmqpException.
            //
            // A detach without an error-condition can happen when Service upgrades. Also, while the service often
            // detaches with the error-condition 'com.microsoft:timeout' when there is no session, sometimes,
            // when a free or new session is unavailable, detach can happen without the error-condition.
            .switchIfEmpty(Mono.error(() -> new AmqpException(true,
                "Unable to read session properties. Receive Link completed without being Active.", null)))
            .map(__ -> {
                @SuppressWarnings("unchecked")
                final Map<Symbol, Object> remoteSource = ((Source) receiver.getRemoteSource()).getFilter();
                final Object sessionIdObject = remoteSource.get(SESSION_FILTER);
                if (sessionIdObject == null) {
                    throw logger.atInfo()
                        .log(new AmqpException(false, "Unable to read session properties. There is no session id.",
                            null));
                }
                final OffsetDateTime sessionLockedUntil;
                if (receiver.getRemoteProperties() != null
                    && receiver.getRemoteProperties().containsKey(LOCKED_UNTIL_UTC)) {
                    final long ticks = (long) receiver.getRemoteProperties().get(LOCKED_UNTIL_UTC);
                    sessionLockedUntil = MessageUtils.convertDotNetTicksToOffsetDateTime(ticks);
                } else {
                    logger.info("Locked until not set.");
                    sessionLockedUntil = Instant.EPOCH.atOffset(ZoneOffset.UTC);
                }
                return new SessionProperties(String.valueOf(sessionIdObject), sessionLockedUntil);
            })
            .cache(value -> Duration.ofMillis(Long.MAX_VALUE), error -> Duration.ZERO, () -> Duration.ZERO);
    }

    @Override
    public Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
        return super.updateDisposition(lockToken, deliveryState);
    }

    @Override
    public Flux<Message> receive() {
        return super.receive().publishOn(ReceiversPumpingScheduler.instance());
    }

    @Override
    public Mono<String> getSessionId() {
        return sessionIdMono;
    }

    @Override
    public Mono<OffsetDateTime> getSessionLockedUntil() {
        return sessionLockedUntil;
    }

    @Override
    public Mono<SessionProperties> getSessionProperties() {
        return sessionProperties;
    }

    @Override
    public Mono<Void> closeAsync() {
        return closeAsync("User invoked close operation.", null);
    }

    @Override
    protected Mono<Void> closeAsync(String message, ErrorCondition errorCondition) {
        return super.closeAsync(message, errorCondition);
    }
}
