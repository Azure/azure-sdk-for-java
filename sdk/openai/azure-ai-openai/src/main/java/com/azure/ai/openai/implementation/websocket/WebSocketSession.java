package com.azure.ai.openai.implementation.websocket;

import java.util.function.Consumer;

public interface WebSocketSession {

    boolean isOpen();

    void sendObjectAsync(Object data, Consumer<SendResult> handler);

    void close();

    // following API is for testing
    void sendTextAsync(String text, Consumer<SendResult> handler);

    void closeSocket();
}
