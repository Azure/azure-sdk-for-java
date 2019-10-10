// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.amqp.models.ProxyConfiguration;

import java.time.Duration;

class MockReactorHandlerProvider extends ReactorHandlerProvider {
    private final ConnectionHandler connectionHandler;
    private final SessionHandler sessionHandler;
    private final SendLinkHandler sendLinkHandler;
    private final ReceiveLinkHandler receiveLinkHandler;

    MockReactorHandlerProvider(ReactorProvider provider, ConnectionHandler connectionHandler, SessionHandler sessionHandler,
                               SendLinkHandler sendLinkHandler, ReceiveLinkHandler receiveLinkHandler) {
        super(provider);
        this.connectionHandler = connectionHandler;
        this.sessionHandler = sessionHandler;
        this.sendLinkHandler = sendLinkHandler;
        this.receiveLinkHandler = receiveLinkHandler;
    }

    @Override
    public SessionHandler createSessionHandler(String connectionId, String hostname, String sessionName, Duration openTimeout) {
        return sessionHandler;
    }

    @Override
    public ConnectionHandler createConnectionHandler(String connectionId, String hostname, TransportType transportType,
                                                     ProxyConfiguration configuration) {
        return connectionHandler;
    }

    @Override
    public SendLinkHandler createSendLinkHandler(String connectionId, String hostname, String senderName, String entityPath) {
        return sendLinkHandler;
    }

    @Override
    public ReceiveLinkHandler createReceiveLinkHandler(String connectionId, String hostname, String receiverName, String entityPath) {
        return receiveLinkHandler;
    }
}
