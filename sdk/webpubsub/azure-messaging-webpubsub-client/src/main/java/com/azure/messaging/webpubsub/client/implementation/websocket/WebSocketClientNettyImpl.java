// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.models.ConnectFailedException;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class WebSocketClientNettyImpl implements WebSocketClient {
    @Override
    public WebSocketSession connectToServer(ClientEndpointConfiguration cec, String path,
        AtomicReference<ClientLogger> loggerReference, Consumer<Object> messageHandler,
        Consumer<WebSocketSession> openHandler, Consumer<CloseReason> closeHandler) {
        try {
            WebSocketSessionNettyImpl session = new WebSocketSessionNettyImpl(cec, path, loggerReference,
                messageHandler, openHandler, closeHandler);
            session.connect();
            return session;
        } catch (Exception e) {
            throw loggerReference.get().logExceptionAsError(new ConnectFailedException("Failed to connect", e));
        }
    }
}
