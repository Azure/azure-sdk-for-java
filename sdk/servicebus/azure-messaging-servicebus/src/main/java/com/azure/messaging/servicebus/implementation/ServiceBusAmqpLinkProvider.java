// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import org.apache.qpid.proton.engine.Receiver;

import java.time.Duration;

/**
 * Provides links for send and receive.
 */
public final class ServiceBusAmqpLinkProvider extends AmqpLinkProvider {
    /**
     * Creates an Amqp Link to receive messages from a Service Bus entity.
     *
     * @param connection The connection to host the Amqp Link.
     * @param entityPath The message broker address for the receiver.
     * @param receiver The underlying QPID receiver.
     * @param handler The QPID handler associated with the QPID receiver.
     * @param tokenManager Token manager for authorising with the CBS node.
     * @param reactorProvider The provider for QPID Reactor and Reactor Executor.
     * @param timeout The timeout for message disposition operations.
     * @param retryPolicy The Retry policy.
     * @return An Amqp Link.
     */
    public ServiceBusReactorReceiver createReceiveLink(AmqpConnection connection, String entityPath, Receiver receiver,
        ReceiveLinkHandler handler, TokenManager tokenManager, ReactorProvider reactorProvider, Duration timeout,
        AmqpRetryPolicy retryPolicy) {
        return new ServiceBusReactorReceiver(connection, entityPath, receiver, handler, tokenManager, reactorProvider,
            timeout, retryPolicy);
    }
}
