// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Map;

public class ServiceBusMultiSessionReceiverAsyncClient implements AutoCloseable {
    public ByteBuffer getSessionState(String sessionId) { return null;}
    public void setSessionState(String sessionId, ByteBuffer sessionState) {}
    public Mono<Void> abandon(MessageLockToken lockToken) { return null;}
    public Mono<Void> abandon(MessageLockToken lockToken, Map<String, Object> propertiesToModify) {return null;}
    public Mono<Void> complete(MessageLockToken lockToken) {return null; }
    public Mono<Void> defer(MessageLockToken lockToken) {return null; }
    public Mono<Void> defer(MessageLockToken lockToken, Map<String, Object> propertiesToModify) {return null; }
    public Mono<Void> deadLetter(MessageLockToken lockToken) {return null; }
    public Mono<Void> deadLetter(MessageLockToken lockToken, DeadLetterOptions deadLetterOptions) {return null; }

    public Flux<ServiceBusReceivedMessage> receive() {
        return null;
    }
    public Flux<ServiceBusReceivedMessage> receive(ReceiveAsyncOptions options) {
        return null;
    }
    /**
     * Disposes of the {@link ServiceBusMultiSessionReceiverAsyncClient}. If the client had a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {

    }
}
