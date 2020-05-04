// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.SessionReceiver;
import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * Package-private class that manages session aware message receiving.
 */
class SessionManager implements AutoCloseable {
    private final ClientLogger logger = new ClientLogger(SessionManager.class);
    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ReceiverOptions receiverOptions;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final Duration operationTimeout;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final List<Scheduler> schedulers;
    private final Map<String, Integer> sessionIdToUpstreamMap = new HashMap<>();

    private final EmitterProcessor<Flux<ServiceBusReceivedMessageContext>> processor = EmitterProcessor.create();
    private final FluxSink<Flux<ServiceBusReceivedMessageContext>> sessionReceiveSink = processor.sink();
    private final Flux<ServiceBusReceivedMessageContext> receiveFlux = Flux.merge(processor);

    SessionManager(String entityPath, MessagingEntityType entityType, ServiceBusConnectionProcessor connectionProcessor,
        Duration operationTimeout, TracerProvider tracerProvider, MessageSerializer messageSerializer,
        ReceiverOptions receiverOptions) {

        this.entityPath = entityPath;
        this.entityType = entityType;
        this.receiverOptions = receiverOptions;
        this.connectionProcessor = connectionProcessor;
        this.operationTimeout = operationTimeout;
        this.tracerProvider = tracerProvider;
        this.messageSerializer = messageSerializer;

        // According to the documentation, if a sequence is not finite, it should be published on their own scheduler.
        // It's possible that some of these sessions have a lot of messages.
        final int numberSchedulers = receiverOptions.isRollingSessionReceiver()
            ? receiverOptions.getMaxConcurrentSessions()
            : 1;

        final List<Scheduler> schedulerList = IntStream.range(0, receiverOptions.getMaxConcurrentSessions())
            .mapToObj(index -> Schedulers.newBoundedElastic(DEFAULT_BOUNDED_ELASTIC_SIZE,
                DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "receiver-" + index))
            .collect(Collectors.toList());

        this.schedulers = Collections.unmodifiableList(schedulerList);
    }

    /**
     * Gets a stream of messages from different sessions.
     *
     * @return A Flux of messages merged from different sessions.
     */
    Flux<ServiceBusReceivedMessageContext> receive(ReceiveAsyncOptions options) {
        return receiveFlux;
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        upstreams.forEach(processor -> processor.cancel());
        upstreams.clear();

        for (Scheduler scheduler : schedulers) {
            scheduler.dispose();
        }
    }

    private Mono<SessionReceiver> getSession(ReceiveAsyncOptions options) {
        Mono<AmqpReceiveLink> amqpReceiveLink = getActiveLink();

    }

    private Mono<AmqpReceiveLink> getActiveLink() {
        final String linkName = StringUtil.getRandomString("session-");

        createReceiveLink(linkName).flatMap(link -> {
            return link.getEndpointStates()
                .takeUntil(e -> e == AmqpEndpointState.ACTIVE)
                .timeout(operationTimeout)
                .onErrorResume(TimeoutException.class, error -> {
                    logger.info("")
                    return Mono.empty();
                });

        });
    }

    private Mono<AmqpReceiveLink> createReceiveLink(String linkName) {
        return connectionProcessor
            .flatMap(connection -> connection.createReceiveLink(linkName, entityPath, receiverOptions.getReceiveMode(),
                null, entityType, null));
    }

}
