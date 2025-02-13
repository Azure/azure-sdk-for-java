// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime.implementation.websocket;

import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface WebSocketClient {

    WebSocketSession connectToServer(ClientEndpointConfiguration cec,
        Supplier<AuthenticationProvider.AuthenticationHeader> authenticationHeaderSupplier,
        Consumer<Object> messageHandler, Consumer<WebSocketSession> openHandler, Consumer<CloseReason> closeHandler);
}
