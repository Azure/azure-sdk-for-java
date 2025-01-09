// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import com.azure.ai.openai.realtime.models.ConnectFailedException;
import com.azure.core.util.logging.ClientLogger;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class WebSocketClientNettyImpl implements WebSocketClient {
    @Override
    public WebSocketSession connectToServer(ClientEndpointConfiguration cec,
        Supplier<AuthenticationProvider.AuthenticationHeader> authenticationHeaderSupplier,
        AtomicReference<ClientLogger> loggerReference, Consumer<Object> messageHandler,
        Consumer<WebSocketSession> openHandler, Consumer<CloseReason> closeHandler) {
        try {
            WebSocketSessionNettyImpl session = new WebSocketSessionNettyImpl(cec, authenticationHeaderSupplier,
                loggerReference, messageHandler, openHandler, closeHandler);
            session.connect();
            return session;
        } catch (Exception e) {
            throw loggerReference.get().logExceptionAsError(new ConnectFailedException("Failed to connect", e));
        }
    }
}
