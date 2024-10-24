// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.ConsumerFactory;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ProtonSessionWrapper;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorSession;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.DeliverySettleMode;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnsignedInteger;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.implementation.AmqpConstants.CLIENT_IDENTIFIER;
import static com.azure.core.amqp.implementation.AmqpConstants.CLIENT_RECEIVER_IDENTIFIER;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.messaging.servicebus.implementation.MessageUtils.adjustServerTimeout;

/**
 * An AMQP session for Service Bus.
 */
class ServiceBusReactorSession extends ReactorSession implements ServiceBusSession {
    static final Symbol SESSION_FILTER = Symbol.getSymbol(AmqpConstants.VENDOR + ":session-filter");
    static final Symbol LOCKED_UNTIL_UTC = Symbol.getSymbol(AmqpConstants.VENDOR + ":locked-until-utc");

    private static final Symbol LINK_TIMEOUT_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":timeout");
    private static final Symbol ENTITY_TYPE_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR + ":entity-type");
    private static final Symbol LINK_TRANSFER_DESTINATION_PROPERTY = Symbol.getSymbol(AmqpConstants.VENDOR
        + ":transfer-destination-address");

    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReactorSession.class);
    private final AmqpRetryPolicy retryPolicy;
    private final ServiceBusAmqpLinkProvider linkProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final Mono<ClaimsBasedSecurityNode> cbsNodeSupplier;
    private final AmqpConnection amqpConnection;
    private final AmqpRetryOptions retryOptions;
    private final boolean distributedTransactionsSupport;
    private final boolean isV2;

    /**
     * Creates a new AMQP session using proton-j.
     *
     * @param session Proton-j session for this AMQP session.
     * @param handlerProvider Providers reactor handlers for listening to proton-j reactor events.
     * @param linkProvider Provides amqp links for send and receive.
     * @param cbsNodeSupplier Mono that returns a reference to the {@link ClaimsBasedSecurityNode}.
     * @param tokenManagerProvider Provides {@link TokenManager} that authorizes the client when performing
     *     operations on the message broker.
     * @param retryOptions Retry options.
     * @param createOptions  the options to create {@link ServiceBusReactorSession}.
     * @param isV2 (temporary) flag indicating which receiver, v1 or v2, to create.
     */
    ServiceBusReactorSession(AmqpConnection amqpConnection, ProtonSessionWrapper session,
        ReactorHandlerProvider handlerProvider, ServiceBusAmqpLinkProvider linkProvider,
        Mono<ClaimsBasedSecurityNode> cbsNodeSupplier, TokenManagerProvider tokenManagerProvider,
        MessageSerializer messageSerializer, AmqpRetryOptions retryOptions,
        ServiceBusCreateSessionOptions createOptions, boolean isV2) {
        super(amqpConnection, session, handlerProvider, linkProvider, cbsNodeSupplier, tokenManagerProvider,
            messageSerializer, retryOptions);
        this.amqpConnection = amqpConnection;
        this.retryOptions = retryOptions;
        this.linkProvider = linkProvider;
        this.retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
        this.tokenManagerProvider = tokenManagerProvider;
        this.cbsNodeSupplier = cbsNodeSupplier;
        this.distributedTransactionsSupport = createOptions.isDistributedTransactionsSupported();
        this.isV2 = isV2;
    }

    @Override
    public Mono<ServiceBusReceiveLink> createConsumer(String linkName, String entityPath,
            MessagingEntityType entityType, Duration timeout, AmqpRetryPolicy retry, ServiceBusReceiveMode receiveMode,
            String clientIdentifier) {
        final Map<Symbol, Object> filter = new HashMap<>();

        return createConsumer(linkName, entityPath, entityType, timeout, retry, receiveMode, filter, clientIdentifier);
    }

    @Override
    public Mono<ServiceBusReceiveLink> createConsumer(String linkName, String entityPath,
        MessagingEntityType entityType, Duration timeout, AmqpRetryPolicy retry, ServiceBusReceiveMode receiveMode,
        String clientIdentifier, String sessionId) {

        final Map<Symbol, Object> filter = new HashMap<>();
        filter.put(SESSION_FILTER, sessionId);

        return createConsumer(linkName, entityPath, entityType, timeout, retry, receiveMode, filter, clientIdentifier);
    }

    @Override
    public Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout,
        AmqpRetryPolicy retry, String transferEntityPath, String clientIdentifier) {
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        Objects.requireNonNull(retry, "'retry' cannot be null.");

        final Duration serverTimeout = adjustServerTimeout(timeout);
        Map<Symbol, Object> linkProperties = new HashMap<>();

        linkProperties.put(LINK_TIMEOUT_PROPERTY, UnsignedInteger.valueOf(serverTimeout.toMillis()));
        linkProperties.put(CLIENT_IDENTIFIER, clientIdentifier);
        if (!CoreUtils.isNullOrEmpty(transferEntityPath)) {
            linkProperties.put(LINK_TRANSFER_DESTINATION_PROPERTY, transferEntityPath);

            LOGGER.atVerbose()
                .addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue("transferEntityPath", transferEntityPath)
                .log("Get or create sender link.");

            final TokenManager tokenManager = tokenManagerProvider.getTokenManager(cbsNodeSupplier,
                transferEntityPath);

            return tokenManager.authorize()
                .doFinally(signalType -> tokenManager.close())
                .then(createProducer(linkName, entityPath, timeout, retry, linkProperties));
        } else {
            LOGGER.atVerbose()
                .addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log("Get or create sender link.");

            return createProducer(linkName, entityPath, timeout, retry, linkProperties);
        }
    }

    @Override
    public Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout, AmqpRetryPolicy retry) {
        return this.createProducer(linkName, entityPath, timeout, retry, (Map<Symbol, Object>) null);
    }

    @Override
    protected Mono<AmqpLink> createProducer(String linkName, String entityPath, Duration timeout,
        AmqpRetryPolicy retry, Map<Symbol, Object> linkProperties) {
        if (distributedTransactionsSupport) {
            return getOrCreateTransactionCoordinator().flatMap(coordinator -> super.createProducer(linkName, entityPath,
                timeout, retry, linkProperties));
        } else {
            return super.createProducer(linkName, entityPath, timeout, retry, linkProperties);
        }
    }

    private Mono<ServiceBusReceiveLink> createConsumer(String linkName, String entityPath,
        MessagingEntityType entityType, Duration timeout, AmqpRetryPolicy retry, ServiceBusReceiveMode receiveMode,
        Map<Symbol, Object> filter, String clientIdentifier) {
        Objects.requireNonNull(linkName, "'linkName' cannot be null.");
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        Objects.requireNonNull(retry, "'retry' cannot be null.");
        Objects.requireNonNull(receiveMode, "'receiveMode' cannot be null.");

        final Map<Symbol, Object> linkProperties = new HashMap<>();
        final Duration serverTimeout = adjustServerTimeout(timeout);
        linkProperties.put(LINK_TIMEOUT_PROPERTY, UnsignedInteger.valueOf(serverTimeout.toMillis()));
        linkProperties.put(CLIENT_RECEIVER_IDENTIFIER, clientIdentifier);
        if (entityType != null) {
            linkProperties.put(ENTITY_TYPE_PROPERTY, entityType.getValue());
        }

        final SenderSettleMode senderSettleMode;
        final ReceiverSettleMode receiverSettleMode;
        final DeliverySettleMode deliverySettleMode;
        switch (receiveMode) {
            case PEEK_LOCK:
                senderSettleMode = SenderSettleMode.UNSETTLED;
                receiverSettleMode = ReceiverSettleMode.SECOND;
                deliverySettleMode = DeliverySettleMode.SETTLE_VIA_DISPOSITION;
                break;
            case RECEIVE_AND_DELETE:
                senderSettleMode = SenderSettleMode.SETTLED;
                receiverSettleMode = ReceiverSettleMode.FIRST;
                deliverySettleMode = DeliverySettleMode.ACCEPT_AND_SETTLE_ON_DELIVERY;
                break;
            default:
                return Mono.error(new RuntimeException("ReceiveMode is not supported: " + receiveMode));
        }

        final ConsumerFactory consumerFactory;
        if (isV2) {
            consumerFactory = new ConsumerFactory(deliverySettleMode, true);
        } else {
            consumerFactory = new ConsumerFactory();
        }

        if (distributedTransactionsSupport) {
            return getOrCreateTransactionCoordinator().flatMap(transactionCoordinator -> super.createConsumer(linkName,
                entityPath, timeout, retry, filter, linkProperties, null, senderSettleMode,
                receiverSettleMode, consumerFactory)
                .cast(ServiceBusReceiveLink.class));
        } else {
            return super.createConsumer(linkName, entityPath, timeout, retry, filter, linkProperties,
                null, senderSettleMode, receiverSettleMode, consumerFactory).cast(ServiceBusReceiveLink.class);
        }
    }
}
