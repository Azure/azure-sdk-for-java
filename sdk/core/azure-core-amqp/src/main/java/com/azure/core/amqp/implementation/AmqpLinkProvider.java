// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import reactor.core.scheduler.Scheduler;

/**
 * Provides links for send and receive.
 *
 * Under normal execution, the provider provides the actual links, but when running under unit test scenarios,
 * the provider enables tests to inject mock links.
 * 
 * @see ReactorProvider
 * @see ReactorHandlerProvider
 * @see AmqpMetricsProvider
 */
public class AmqpLinkProvider {
    /**
     * Creates an Amqp Link to send messages.
     *
     * @param amqpConnection The connection to host the Amqp Link.
     * @param entityPath The message broker address for the sender.
     * @param sender The underlying QPID sender.
     * @param handler The QPID handler associated with the QPID sender.
     * @param reactorProvider The provider for QPID Reactor and Reactor Executor.
     * @param tokenManager Token manager for authorising with the CBS node.
     * @param messageSerializer the Serializer to deserialize and serialize AMQP messages.
     * @param retryOptions The Retry options.
     * @param scheduler The scheduler to time out the send operations those are not acknowledged by the broker.
     * @param metricsProvider The metric provider (e.g. to record operations such as send).
     * @return An Amqp Link.
     */
    public AmqpSendLink createSendLink(AmqpConnection amqpConnection, String entityPath, Sender sender,
        SendLinkHandler handler, ReactorProvider reactorProvider, TokenManager tokenManager,
        MessageSerializer messageSerializer, AmqpRetryOptions retryOptions, Scheduler scheduler,
        AmqpMetricsProvider metricsProvider) {
        return new ReactorSender(amqpConnection, entityPath, sender, handler, reactorProvider, tokenManager,
            messageSerializer, retryOptions, scheduler, metricsProvider);
    }

    /**
     * Creates an Amqp Link to receive messages.
     *
     * @param amqpConnection The connection to host the Amqp Link.
     * @param entityPath The message broker address for the receiver.
     * @param receiver The underlying QPID receiver.
     * @param handler The QPID handler associated with the QPID receiver.
     * @param tokenManager Token manager for authorising with the CBS node.
     * @param dispatcher The dispatcher to schedule work to QPID Reactor Executor.
     * @param retryOptions The Retry options.
     * @param metricsProvider The metric provider (e.g. to record operations such as sending flow).
     * @return An Amqp Link.
     */
    public AmqpReceiveLink createReceiveLink(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
        ReceiveLinkHandler handler, TokenManager tokenManager, ReactorDispatcher dispatcher,
        AmqpRetryOptions retryOptions, AmqpMetricsProvider metricsProvider) {
        return new ReactorReceiver(amqpConnection, entityPath, receiver, new ReceiveLinkHandlerWrapper(handler),
            tokenManager, dispatcher, retryOptions, metricsProvider);
    }

    // Note: ReceiveLinkHandler2 will become the ReceiveLinkHandler once the side by side support for v1 and v2 stack
    // is removed. At that point "ReceiveLinkHandlerWrapper" and this createReceiveLink method will also be removed.

    /**
     * Creates an Amqp Link to receive messages.
     *
     * @param amqpConnection The connection to host the Amqp Link.
     * @param entityPath The message broker address for the receiver.
     * @param receiver The underlying QPID receiver.
     * @param handler The QPID handler associated with the QPID receiver.
     * @param tokenManager Token manager for authorising with the CBS node.
     * @param dispatcher The dispatcher to schedule work to QPID Reactor Executor.
     * @param retryOptions The Retry options.
     * @param metricsProvider The metric provider (e.g. to record operations such as sending flow).
     * @return An Amqp Link.
     */
    public AmqpReceiveLink createReceiveLink(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
        ReceiveLinkHandler2 handler, TokenManager tokenManager, ReactorDispatcher dispatcher,
        AmqpRetryOptions retryOptions, AmqpMetricsProvider metricsProvider) {
        return new ReactorReceiver(amqpConnection, entityPath, receiver, new ReceiveLinkHandlerWrapper(handler),
            tokenManager, dispatcher, retryOptions, metricsProvider);
    }
}
