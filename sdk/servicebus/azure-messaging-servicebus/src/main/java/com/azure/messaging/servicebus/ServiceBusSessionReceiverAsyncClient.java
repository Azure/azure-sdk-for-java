// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.messaging.servicebus.implementation.MessageLockContainer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Instant;

public class ServiceBusSessionReceiverAsyncClient extends ServiceBusReceiverAsyncClient {
    /**
     * Creates a receiver that listens to a Service Bus resource.
     * @param fullyQualifiedNamespace The fully qualified domain name for the Service Bus resource.
     * @param entityPath              The name of the topic or queue.
     * @param entityType              The type of the Service Bus resource.
     * @param receiverOptions         Options when receiving messages.
     * @param connectionProcessor     The AMQP connection to the Service Bus resource.
     * @param tracerProvider          Tracer for telemetry.
     * @param messageSerializer       Serializes and deserializes Service Bus messages.
     * @param messageLockContainer    Container for message locks.
     * @param onClientClose           Operation to run when the client completes.
     */
    ServiceBusSessionReceiverAsyncClient(String fullyQualifiedNamespace, String entityPath, MessagingEntityType entityType, ReceiverOptions receiverOptions, ServiceBusConnectionProcessor connectionProcessor, TracerProvider tracerProvider, MessageSerializer messageSerializer, MessageLockContainer messageLockContainer, Runnable onClientClose) {
        super(fullyQualifiedNamespace, entityPath, entityType, receiverOptions, connectionProcessor, tracerProvider, messageSerializer, messageLockContainer, onClientClose);
    }
    public Mono<Instant> renewSessionLock(MessageLockToken lockToken){return null;};
    public Mono<ByteBuffer> getSessionState() {return null;}
    public Mono<Void> setSessionState(ByteBuffer sessionState) {return null;}
    public String getSessionId(){ return null;}
    public Mono<Instant> getSessionLockedUntil() {return null;}

}
