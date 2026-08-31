// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ServerSentEventListenerTests {
    @Test
    public void onEventReceivesEvent() {
        AtomicReference<ServerSentEvent<String>> receivedEvent = new AtomicReference<>();
        ServerSentEventListener<String> listener = receivedEvent::set;
        ServerSentEvent<String> event = new ServerSentEvent<>(null, "message", "payload", null, null);

        listener.onEvent(event);

        assertSame(event, receivedEvent.get());
    }

    @Test
    public void defaultCallbacksDoNothing() {
        ServerSentEventListener<String> listener = event -> {
        };

        assertDoesNotThrow(() -> {
            listener.onError(new IllegalStateException("error"));
            listener.onClose();
        });
    }
}
