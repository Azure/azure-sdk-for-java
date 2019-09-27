// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.RetryPolicy;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorSession;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.MessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;
import static com.azure.core.amqp.implementation.AmqpConstants.VENDOR;

/**
 * An AMQP session for Event Hubs.
 */
class EventHubReactorSession extends ReactorSession implements EventHubSession {
    private static final Symbol EPOCH = Symbol.valueOf(VENDOR + ":epoch");
    private static final Symbol RECEIVER_IDENTIFIER_NAME = Symbol.valueOf(VENDOR + ":receiver-name");
    private static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME =
        Symbol.valueOf(VENDOR + ":enable-receiver-runtime-metric");

    /**
     * Creates a new AMQP session using proton-j.
     *
     * @param session Proton-j session for this AMQP session.
     * @param sessionHandler Handler for events that occur in the session.
     * @param sessionName Name of the session.
     * @param provider Provides reactor instances for messages to sent with.
     * @param handlerProvider Providers reactor handlers for listening to proton-j reactor events.
     * @param cbsNodeSupplier Mono that returns a reference to the {@link CBSNode}.
     * @param tokenManagerProvider Provides {@link TokenManager} that authorizes the client when performing
     *     operations on the message broker.
     * @param openTimeout Timeout to wait for the session operation to complete.
     */
    EventHubReactorSession(Session session, SessionHandler sessionHandler, String sessionName,
                           ReactorProvider provider, ReactorHandlerProvider handlerProvider,
                           Mono<CBSNode> cbsNodeSupplier, TokenManagerProvider tokenManagerProvider,
                           Duration openTimeout, MessageSerializer messageSerializer) {
        super(session, sessionHandler, sessionName, provider, handlerProvider, cbsNodeSupplier, tokenManagerProvider,
            messageSerializer, openTimeout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout, RetryPolicy retry,
                                                EventPosition eventPosition, EventHubConsumerOptions options) {
        Objects.requireNonNull(linkName, "'linkName' cannot be null.");
        Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(timeout, "'timeout' cannot be null.");
        Objects.requireNonNull(retry, "'retry' cannot be null.");
        Objects.requireNonNull(eventPosition, "'eventPosition' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        //TODO (conniey): support creating a filter when we've already received some events. I believe this in
        // the cause of recreating a failing link.
        // final Map<Symbol, UnknownDescribedType> filterMap = MessageReceiver.this.settingsProvider
        // .getFilter(MessageReceiver.this.lastReceivedMessage);
        // if (filterMap != null) {
        //    source.setFilter(filterMap);
        // }
        final String eventPositionExpression = getExpression(eventPosition);
        final Map<Symbol, UnknownDescribedType> filter = new HashMap<>();
        filter.put(AmqpConstants.STRING_FILTER, new UnknownDescribedType(AmqpConstants.STRING_FILTER,
            eventPositionExpression));

        final Map<Symbol, Object> properties = new HashMap<>();
        if (options.getOwnerLevel() != null) {
            properties.put(EPOCH, options.getOwnerLevel());
        }
        if (!ImplUtils.isNullOrEmpty(options.getIdentifier())) {
            properties.put(RECEIVER_IDENTIFIER_NAME, options.getIdentifier());
        }

        final Symbol[] desiredCapabilities = options.getLastEnqueuedEventProperties()
            ? new Symbol[]{ENABLE_RECEIVER_RUNTIME_METRIC_NAME}
            : null;

        return createConsumer(linkName, entityPath, timeout, retry, filter, properties, desiredCapabilities);
    }

    private static String getExpression(EventPosition eventPosition) {
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
                ms = Long.toString(Long.MAX_VALUE);
            }

            return String.format(
                AmqpConstants.AMQP_ANNOTATION_FORMAT,
                ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue(),
                isInclusiveFlag,
                ms);
        }

        throw new IllegalArgumentException("No starting position was set.");
    }
}
