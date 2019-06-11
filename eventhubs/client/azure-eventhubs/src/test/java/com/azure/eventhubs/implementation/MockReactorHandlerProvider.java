// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.TransportType;
import com.azure.eventhubs.implementation.handler.ConnectionHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;

import java.time.Duration;

class MockReactorHandlerProvider extends ReactorHandlerProvider {
    private final ConnectionHandler connectionHandler;
    private final SessionHandler sessionHandler;

    MockReactorHandlerProvider(ReactorProvider provider, ConnectionHandler connectionHandler, SessionHandler sessionHandler) {
        super(provider);
        this.connectionHandler = connectionHandler;
        this.sessionHandler = sessionHandler;
    }

    @Override
    SessionHandler createSessionHandler(String connectionId, String host, String sessionName, Duration openTimeout) {
        return sessionHandler;
    }

    @Override
    ConnectionHandler createConnectionHandler(String connectionId, String hostname, TransportType transportType) {
        return connectionHandler;
    }
}
