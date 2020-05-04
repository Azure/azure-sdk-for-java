// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.MessageLockToken;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class SessionReceiver {
    private final ServiceBusMessageProcessor processor;
    private final ServiceBusReceiveLinkProcessor linkProcessor;
    private final ServiceBusManagementNode managementNode;
    private final AmqpRetryOptions retryOptions;
    private final AtomicReference<Instant> currentExpiry = new AtomicReference<>();
    private final AtomicReference<String> sessionId = new AtomicReference<>();
    private final ClientLogger logger = new ClientLogger(SessionReceiver.class);

    public SessionReceiver(ServiceBusReceiveLinkProcessor linkProcessor, ServiceBusManagementNode managementNode,
        MessageSerializer messageSerializer, boolean isAutoComplete, boolean autoLockRenewal,
        Duration maxAutoLockRenewDuration, AmqpRetryOptions retryOptions) {
        this.linkProcessor = linkProcessor;
        this.managementNode = managementNode;
        this.retryOptions = retryOptions;
        this.processor = linkProcessor
            .map(message -> messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .subscribeWith(new ServiceBusMessageProcessor(isAutoComplete, autoLockRenewal, maxAutoLockRenewDuration,
                retryOptions, linkProcessor.getErrorContext(), this::complete, this::abandon,
                SessionReceiver::renewMessageLock));

        processor.next().subscribe(first -> {
            logger.info("Setting session id: {}", first.getSessionId());

            if (!sessionId.compareAndSet(null, first.getSessionId())) {
                logger.warning("Session id already set. Existing: {}", sessionId.get());
            }
        });
    }

    public Flux<ServiceBusReceivedMessage> receive() {
        return processor;
    }

    public Mono<Void> setSessionState(String sessionId, byte[] state) {
        return managementNode.setSessionState(sessionId, state, linkProcessor.getLinkName());
    }

    public Mono<byte[]> getSessionState() {
        return managementNode.getSessionState(sessionId.get(), linkProcessor.getLinkName());
    }

    public Mono<Void> renewSessionLock() {
        return managementNode.renewSessionLock()
    }

    private Mono<Void> complete(MessageLockToken token) {
        return managementNode.updateDisposition(token.getLockToken(), DispositionStatus.COMPLETED, null,
            null, null, sessionId.get(), linkProcessor.getLinkName());
    }

    private Mono<Void> abandon(MessageLockToken token) {
        return managementNode.updateDisposition(token.getLockToken(), DispositionStatus.ABANDONED, null,
            null, null, sessionId.get(), linkProcessor.getLinkName());
    }

    /**
     * Adding a stub renew message lock since sessions lock all messages.
     */
    private static Mono<Instant> renewMessageLock(MessageLockToken token) {
        return Mono.just(Instant.now().plusSeconds(60));
    }
}
