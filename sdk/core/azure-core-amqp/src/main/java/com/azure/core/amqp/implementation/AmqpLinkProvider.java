// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import reactor.core.scheduler.Scheduler;

/**
 * Provides links for send and receive.
 */
public class AmqpLinkProvider {

    public AmqpSendLink createSendLink(AmqpConnection amqpConnection, String entityPath, Sender sender, SendLinkHandler handler,
        ReactorProvider reactorProvider, TokenManager tokenManager, MessageSerializer messageSerializer,
        AmqpRetryOptions retryOptions, Scheduler scheduler, AmqpMetricsProvider metricsProvider) {
        return new ReactorSender(amqpConnection, entityPath, sender, handler, reactorProvider, tokenManager,
            messageSerializer, retryOptions, scheduler, metricsProvider);
    }

    public AmqpReceiveLink createReceiveLink(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
        ReceiveLinkHandler handler, TokenManager tokenManager, ReactorDispatcher dispatcher,
        AmqpRetryOptions retryOptions, AmqpMetricsProvider metricsProvider) {
        return new ReactorReceiver(amqpConnection, entityPath, receiver, handler, tokenManager, dispatcher, retryOptions,
            metricsProvider);
    }
}
