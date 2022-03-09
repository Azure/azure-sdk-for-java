// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorSession;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.AmqpMessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.implementation.AmqpConstants.VENDOR;

/**
 * An AMQP session for Event Hubs.
 */
class EventHubReactorSession extends ReactorSession implements EventHubSession {
    private static final Symbol EPOCH = Symbol.valueOf(VENDOR + ":epoch");
    private static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME =
        Symbol.valueOf(VENDOR + ":enable-receiver-runtime-metric");

    private final ClientLogger logger = new ClientLogger(EventHubReactorSession.class);

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
     * @param retryOptions to be used for this session.
     * @param messageSerializer to be used.
     */
    EventHubReactorSession(AmqpConnection amqpConnection, Session session, SessionHandler sessionHandler,
        String sessionName, ReactorProvider provider, ReactorHandlerProvider handlerProvider,
        Mono<ClaimsBasedSecurityNode> cbsNodeSupplier, TokenManagerProvider tokenManagerProvider,
        AmqpRetryOptions retryOptions, MessageSerializer messageSerializer) {
        super(amqpConnection, session, sessionHandler, sessionName, provider, handlerProvider, cbsNodeSupplier,
            tokenManagerProvider, messageSerializer, retryOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout,
            AmqpRetryPolicy retry, EventPosition eventPosition, ReceiveOptions options) {
        Objects.requireNonNull(linkName, "'linkName' cannot be null.");
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        Objects.requireNonNull(retry, "'retry' cannot be null.");
        Objects.requireNonNull(eventPosition, "'eventPosition' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        final String eventPositionExpression = getExpression(eventPosition);
        final Map<Symbol, Object> filter = new HashMap<>();
        filter.put(AmqpConstants.STRING_FILTER, new UnknownDescribedType(AmqpConstants.STRING_FILTER,
            eventPositionExpression));

        final Map<Symbol, Object> properties = new HashMap<>();
        if (options.getOwnerLevel() != null) {
            properties.put(EPOCH, options.getOwnerLevel());
        }

        final Symbol[] desiredCapabilities = options.getTrackLastEnqueuedEventProperties()
            ? new Symbol[]{ENABLE_RECEIVER_RUNTIME_METRIC_NAME}
            : null;

        // Use explicit settlement via dispositions (not pre-settled)
        return createConsumer(linkName, entityPath, timeout, retry, filter, properties, desiredCapabilities,
            SenderSettleMode.UNSETTLED, ReceiverSettleMode.SECOND);
    }

    private String getExpression(EventPosition eventPosition) {
        final String isInclusiveFlag = eventPosition.isInclusive() ? "=" : "";

        // order of preference
        if (eventPosition.getOffset() != null) {
            return String.format(
                AmqpConstants.AMQP_ANNOTATION_FORMAT, OFFSET_ANNOTATION_NAME.getValue(),
                isInclusiveFlag,
                eventPosition.getOffset());
        }

        if (eventPosition.getSequenceNumber() != null) {
            return String.format(
                AmqpConstants.AMQP_ANNOTATION_FORMAT,
                SEQUENCE_NUMBER_ANNOTATION_NAME.getValue(),
                isInclusiveFlag,
                eventPosition.getSequenceNumber());
        }

        if (eventPosition.getEnqueuedDateTime() != null) {
            String ms;
            try {
                ms = Long.toString(eventPosition.getEnqueuedDateTime().toEpochMilli());
            } catch (ArithmeticException ex) {
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.ROOT,
                    "Event position for enqueued DateTime could not be parsed. Value: '%s'",
                    eventPosition.getEnqueuedDateTime()), ex));
            }

            return String.format(AmqpConstants.AMQP_ANNOTATION_FORMAT,
                ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(), isInclusiveFlag, ms);
        }

        throw logger.logExceptionAsError(new IllegalArgumentException("No starting position was set."));
    }
}
