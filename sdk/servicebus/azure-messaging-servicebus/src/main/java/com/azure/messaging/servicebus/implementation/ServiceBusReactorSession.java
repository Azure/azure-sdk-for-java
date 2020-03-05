// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorSession;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An AMQP session for Service Bus.
 */
class ServiceBusReactorSession extends ReactorSession implements ServiceBusSession {

    private final ClientLogger logger = new ClientLogger(ServiceBusReactorSession.class);

    /**
     * Creates a new AMQP session using proton-j.
     *
     * @param session Proton-j session for this AMQP session.
     * @param sessionHandler Handler for events that occur in the session.
     * @param sessionName Name of the session.
     * @param provider Provides reactor instances for messages to sent with.
     * @param handlerProvider Providers reactor handlers for listening to proton-j reactor events.
     * @param cbsNodeSupplier Mono that returns a reference to the {@link ClaimsBasedSecurityNode}.
     * @param tokenManagerProvider Provides {@link TokenManager} that authorizes the client when performing
     *     operations on the message broker.
     * @param openTimeout Timeout to wait for the session operation to complete.
     */
    ServiceBusReactorSession(Session session, SessionHandler sessionHandler, String sessionName,
                             ReactorProvider provider, ReactorHandlerProvider handlerProvider,
                             Mono<ClaimsBasedSecurityNode> cbsNodeSupplier, TokenManagerProvider tokenManagerProvider,
                             Duration openTimeout, MessageSerializer messageSerializer) {
        super(session, sessionHandler, sessionName, provider, handlerProvider, cbsNodeSupplier, tokenManagerProvider,
            messageSerializer, openTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout,
                                                AmqpRetryPolicy retry, ReceiveMode receiveMode) {
        Objects.requireNonNull(linkName, "'linkName' cannot be null.");
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        Objects.requireNonNull(retry, "'retry' cannot be null.");
        Objects.requireNonNull(receiveMode, "'receiveMode' cannot be null.");

        final Map<Symbol, UnknownDescribedType> filter = new HashMap<>();

        final Map<Symbol, Object> properties = new HashMap<>();

        return createConsumer(linkName, entityPath, timeout, retry, filter, properties, null);
    }
}
