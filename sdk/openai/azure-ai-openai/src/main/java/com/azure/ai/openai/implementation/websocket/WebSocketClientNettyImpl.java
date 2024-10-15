package com.azure.ai.openai.implementation.websocket;

import com.azure.ai.openai.models.realtime.ConnectFailedException;
import com.azure.core.util.logging.ClientLogger;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public final class WebSocketClientNettyImpl implements WebSocketClient {
    @Override
    public WebSocketSession connectToServer(ClientEndpointConfiguration cec,
                                            AtomicReference<ClientLogger> loggerReference, Consumer<Object> messageHandler,
                                            Consumer<WebSocketSession> openHandler, Consumer<CloseReason> closeHandler) {
        try {
            WebSocketSessionNettyImpl session = new WebSocketSessionNettyImpl(cec, loggerReference,
                    messageHandler, openHandler, closeHandler);
            session.connect();
            return session;
        } catch (Exception e) {
            throw loggerReference.get().logExceptionAsError(new ConnectFailedException("Failed to connect", e));
        }
    }
}
