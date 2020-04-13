// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorReceiver;
import com.azure.core.amqp.implementation.ReactorSession;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.messaging.servicebus.implementation.MessageUtils.adjustServerTimeout;
/**
 * An AMQP session for Service Bus.
 */
class ServiceBusReactorSession extends ReactorSession implements ServiceBusSession {
    private static final Symbol LINK_TIMEOUT_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":timeout");
    private static final Symbol ENTITY_TYPE_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":entity-type");
    private static final Symbol SESSION_FILTER = Symbol.getSymbol(AmqpConstants.VENDOR + ":session-filter");

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

    @Override
    public Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, MessagingEntityType entityType,
         Duration timeout, AmqpRetryPolicy retry, ReceiveMode receiveMode) {
        return createConsumer(linkName, entityPath, entityType, timeout, retry, receiveMode, null);
    }

    @Override
    public Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, MessagingEntityType entityType,
        Duration timeout, AmqpRetryPolicy retry, ReceiveMode receiveMode, String sessionId) {
        Objects.requireNonNull(linkName, "'linkName' cannot be null.");
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        Objects.requireNonNull(retry, "'retry' cannot be null.");
        Objects.requireNonNull(receiveMode, "'receiveMode' cannot be null.");

        final Map<Symbol, Object> filter = new HashMap<>();

        final Map<Symbol, Object> linkProperties = new HashMap<>();
        final Duration serverTimeout = adjustServerTimeout(timeout);
        linkProperties.put(LINK_TIMEOUT_PROPERTY, UnsignedInteger.valueOf(serverTimeout.toMillis()));
        if (entityType != null) {
            linkProperties.put(ENTITY_TYPE_PROPERTY, entityType.getValue());
        }

        if (!CoreUtils.isNullOrEmpty(sessionId)) {
            filter.put(SESSION_FILTER, sessionId);
        }

        final SenderSettleMode senderSettleMode;
        final ReceiverSettleMode receiverSettleMode;
        switch (receiveMode) {
            case PEEK_LOCK:
                senderSettleMode = SenderSettleMode.UNSETTLED;
                receiverSettleMode = ReceiverSettleMode.SECOND;
                break;
            case RECEIVE_AND_DELETE:
                senderSettleMode = SenderSettleMode.SETTLED;
                receiverSettleMode = ReceiverSettleMode.FIRST;
                break;
            default:
                return Mono.error(new RuntimeException("ReceiveMode is not supported: " + receiveMode));
        }

        return createConsumer(linkName, entityPath, timeout, retry, filter, linkProperties, null, senderSettleMode,
            receiverSettleMode);
    }

    @Override
    protected ReactorReceiver createConsumer(String entityPath, Receiver receiver,
        ReceiveLinkHandler receiveLinkHandler, TokenManager tokenManager, ReactorProvider reactorProvider) {
        return new ServiceBusReactorReceiver(entityPath, receiver, receiveLinkHandler, tokenManager,
            reactorProvider.getReactorDispatcher());
    }
}
