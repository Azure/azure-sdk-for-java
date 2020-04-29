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
    private final AmqpRetryOptions retryOptions;
    private final Function<String, Mono<Instant>> onRenewSessionLock;
    private final AtomicReference<Instant> currentExpiry = new AtomicReference<>();
    private final Mono<String> sessionIdMono;
    private final ClientLogger logger = new ClientLogger(SessionReceiver.class);

    public SessionReceiver(ServiceBusReceiveLinkProcessor linkProcessor,
        MessageSerializer messageSerializer, boolean isAutoComplete, boolean autoLockRenewal,
        Duration maxAutoLockRenewDuration, AmqpRetryOptions retryOptions,
        Function<MessageLockToken, Mono<Void>> onComplete,
        Function<MessageLockToken, Mono<Void>> onAbandon,
        Function<String, Mono<Instant>> onRenewSessionLock) {

        this.retryOptions = retryOptions;
        this.onRenewSessionLock = onRenewSessionLock;
        this.processor = linkProcessor
            .map(message -> messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .subscribeWith(new ServiceBusMessageProcessor(isAutoComplete, autoLockRenewal, maxAutoLockRenewDuration,
                retryOptions, linkProcessor.getErrorContext(), onComplete, onAbandon,
                SessionReceiver::renewMessageLock));

        sessionIdMono = processor.next().map(first -> {
            logger.info("Setting session id: {}", first.getSessionId());
            return first.getSessionId();
        }).cache(e -> Duration.ofMillis(Long.MAX_VALUE),
            error -> {
                logger.error("Error occurred when getting session id.", error);
                return Duration.ZERO;
            }, () -> {
                logger.warning("Completed without emitting a session item.");
                return Duration.ZERO;
            });
    }

    public Flux<ServiceBusReceivedMessage> receive() {
        return processor;
    }

    public Mono<Void> setSessionState(byte[] state) {
        return Mono.empty();
    }

    public Mono<byte[]> getSessionState() {
        return Mono.empty();
    }

    public Mono<Void> renewSessionLock() {
        return Mono.empty();
    }

    /**
     * Adding a stub renew message lock since sessions lock all messages.
     */
    private static Mono<Instant> renewMessageLock(MessageLockToken token) {
        return Mono.just(Instant.now().plusSeconds(60));
    }
}
