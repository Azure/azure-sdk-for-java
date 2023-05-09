// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorReceiver.ReceiveLinkHandlerWrapper;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import org.apache.qpid.proton.engine.Receiver;

/**
 * Provides Service Bus specific links for send and receive.
 *
 * Under normal execution, the provider provides the actual links, but when running under unit test scenarios,
 * the provider enables tests to inject mock links.
 * @see AmqpLinkProvider
 * @see ReactorProvider
 * @see ReactorHandlerProvider
 * @see AmqpMetricsProvider
 */
public final class ServiceBusAmqpLinkProvider extends AmqpLinkProvider {
    @Override
    public AmqpReceiveLink createReceiveLink(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
        ReceiveLinkHandler handler, TokenManager tokenManager, ReactorDispatcher dispatcher, AmqpRetryOptions retryOptions,
        AmqpMetricsProvider metricsProvider) {
        return new ServiceBusReactorReceiver(amqpConnection, entityPath, receiver, new ReceiveLinkHandlerWrapper(handler), tokenManager, dispatcher, retryOptions);
    }

    // Note: ReceiveLinkHandler2 will become the ReceiveLinkHandler once the side by side support for v1 and v2 stack
    // is removed. At that point "ReceiveLinkHandlerWrapper" and this createReceiveLink method will also be removed.
    @Override
    public AmqpReceiveLink createReceiveLink(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
        ReceiveLinkHandler2 handler, TokenManager tokenManager, ReactorDispatcher dispatcher, AmqpRetryOptions retryOptions,
        AmqpMetricsProvider metricsProvider) {
        return new ServiceBusReactorReceiver(amqpConnection, entityPath, receiver, new ReceiveLinkHandlerWrapper(handler), tokenManager, dispatcher, retryOptions);
    }
}
