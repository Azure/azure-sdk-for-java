// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.ws;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.models.ConnectFailedException;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class ClientNettyImpl implements Client {
    @Override
    public Session connectToServer(ClientEndpointConfiguration cec, String path,
                                   AtomicReference<ClientLogger> loggerReference,
                                   Consumer<Object> messageHandler,
                                   Consumer<Session> openHandler,
                                   Consumer<CloseReason> closeHandler) {
        try {
            SessionNettyImpl session = new SessionNettyImpl(cec, path, loggerReference, messageHandler, openHandler, closeHandler);
            session.connect();
            return session;
        } catch (Exception e) {
            throw loggerReference.get().logExceptionAsError(new ConnectFailedException("Failed to connect", e));
        }
    }
}
