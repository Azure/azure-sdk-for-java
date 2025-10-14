// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@FunctionalInterface
public interface WebSocketClient {

    WebSocketSession connectToServer(ClientEndpointConfiguration cec, String path,
        AtomicReference<ClientLogger> loggerReference, Consumer<WebPubSubMessage> messageHandler,
        Consumer<WebSocketSession> openHandler, Consumer<CloseReason> closeHandler);
}
