// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.CBSNode;
import com.azure.core.amqp.RetryPolicy;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.messaging.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.UnknownDescribedType;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class EventHubReactorSession extends ReactorSession implements EventHubSession {
    private static final Symbol EPOCH = Symbol.valueOf(AmqpConstants.VENDOR + ":epoch");
    private static final Symbol RECEIVER_IDENTIFIER_NAME = Symbol.valueOf(AmqpConstants.VENDOR + ":receiver-name");

    public EventHubReactorSession(Session session, SessionHandler sessionHandler, String sessionName,
                                  ReactorProvider provider, ReactorHandlerProvider handlerProvider,
                                  Mono<CBSNode> cbsNodeSupplier, TokenManagerProvider tokenManagerProvider,
                                  Duration openTimeout) {
        super(session, sessionHandler, sessionName, provider, handlerProvider, cbsNodeSupplier, tokenManagerProvider,
            openTimeout);
    }

    @Override
    public Mono<AmqpReceiveLink> createConsumer(String linkName, String entityPath, Duration timeout, RetryPolicy retry,
                                                String eventPositionExpression, Long ownerLevel,
                                                String consumerIdentifier) {

        //TODO (conniey): support creating a filter when we've already received some events. I believe this in
        // the cause of recreating a failing link.
        // final Map<Symbol, UnknownDescribedType> filterMap = MessageReceiver.this.settingsProvider
        // .getFilter(MessageReceiver.this.lastReceivedMessage);
        // if (filterMap != null) {
        //    source.setFilter(filterMap);
        // }
        Map<Symbol, UnknownDescribedType> filter = null;
        if (!ImplUtils.isNullOrEmpty(eventPositionExpression)) {
            filter = new HashMap<>();
            filter.put(AmqpConstants.STRING_FILTER, new UnknownDescribedType(AmqpConstants.STRING_FILTER,
                eventPositionExpression));
        }

        final Map<Symbol, Object> properties = new HashMap<>();
        if (ownerLevel != null) {
            properties.put(EPOCH, ownerLevel);
        }
        if (!ImplUtils.isNullOrEmpty(consumerIdentifier)) {
            properties.put(RECEIVER_IDENTIFIER_NAME, consumerIdentifier);
        }

        //TODO (conniey): After preview 1 feature to enable keeping partition information updated.
        // static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME = Symbol.valueOf(VENDOR +
        // ":enable-receiver-runtime-metric");
        // if (keepPartitionInformationUpdated) {
        //    receiver.setDesiredCapabilities(new Symbol[]{ENABLE_RECEIVER_RUNTIME_METRIC_NAME});
        // }
        return createConsumer(linkName, entityPath, timeout, retry, filter, properties, null);
    }
}
