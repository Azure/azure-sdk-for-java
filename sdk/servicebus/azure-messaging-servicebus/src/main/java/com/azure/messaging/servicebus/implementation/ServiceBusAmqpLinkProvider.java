// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import org.apache.qpid.proton.engine.Receiver;

/**
 * Provides links for send and receive.
 */
public final class ServiceBusAmqpLinkProvider extends AmqpLinkProvider {
    @Override
    public AmqpReceiveLink createReceiveLink(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
        ReceiveLinkHandler handler, TokenManager tokenManager, ReactorDispatcher dispatcher, AmqpRetryOptions retryOptions,
        AmqpMetricsProvider metricsProvider) {
        return new ServiceBusReactorReceiver(amqpConnection, entityPath, receiver, handler, tokenManager, dispatcher, retryOptions);
    }

    @Override
    public AmqpReceiveLink createReceiveLink2(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
        ReceiveLinkHandler2 handler, TokenManager tokenManager, ReactorDispatcher dispatcher, AmqpRetryOptions retryOptions,
        AmqpMetricsProvider metricsProvider) {
        return new ServiceBusReactorReceiver2(amqpConnection, entityPath, receiver, handler, tokenManager, dispatcher, retryOptions);
    }
}
