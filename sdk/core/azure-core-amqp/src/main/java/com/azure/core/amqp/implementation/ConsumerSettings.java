// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.handler.DeliverySettleMode;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Receiver;

import java.util.Objects;

// temporary type to support v1 and v2 side by side.
public final class ConsumerSettings {
    private final boolean isV2;
    private final DeliverySettleMode settleMode;
    private final boolean includeDeliveryTagInMessage;

    public ConsumerSettings() {
        this.isV2 = false;
        this.settleMode = null;
        this.includeDeliveryTagInMessage = false;
    }

    public ConsumerSettings(DeliverySettleMode settlingMode, boolean includeDeliveryTagInMessage) {
        this.isV2 = true;
        this.settleMode = Objects.requireNonNull(settlingMode);
        this.includeDeliveryTagInMessage = includeDeliveryTagInMessage;
    }

    AmqpReceiveLink createConsumer(AmqpConnection amqpConnection, String linkName, String entityPath, Receiver receiver,
        TokenManager tokenManager, ReactorProvider reactorProvider,
        ReactorHandlerProvider handlerProvider, AmqpLinkProvider linkProvider, AmqpRetryOptions retryOptions) {
        final String connectionId = amqpConnection.getId();
        final String hostname = amqpConnection.getFullyQualifiedNamespace();
        final AmqpMetricsProvider metricsProvider = handlerProvider.getMetricProvider(amqpConnection.getFullyQualifiedNamespace(), entityPath);
        if (this.isV2) {
            final ReceiveLinkHandler2 handler = handlerProvider.createReceiveLinkHandler2(connectionId, hostname, linkName, entityPath,
                settleMode, includeDeliveryTagInMessage, reactorProvider.getReactorDispatcher(), retryOptions);
            BaseHandler.setHandler(receiver, handler);
            receiver.open();
            return linkProvider.createReceiveLink(amqpConnection, entityPath, receiver, handler, tokenManager,
                reactorProvider.getReactorDispatcher(), retryOptions, metricsProvider);
        } else {
            final ReceiveLinkHandler handler = handlerProvider.createReceiveLinkHandler(connectionId, hostname, linkName, entityPath);
            BaseHandler.setHandler(receiver, handler);
            receiver.open();
            return linkProvider.createReceiveLink(amqpConnection, entityPath, receiver, handler, tokenManager,
                reactorProvider.getReactorDispatcher(), retryOptions, metricsProvider);
        }
    }
}
