// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;

final class ServiceBusSessionReactorReceiver implements AmqpReceiveLink {
    private final ClientLogger logger;
    private final String sessionId;
    private final AmqpReceiveLink sessionLink;
    private final boolean hasIdleTimeout;
    private final Sinks.Many<Boolean> nextItemIdleTimeoutSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Empty<Void> terminateEndpointStatesSink = Sinks.empty();
    private final Disposable.Composite disposables = Disposables.composite();

    ServiceBusSessionReactorReceiver(ClientLogger logger, ServiceBusTracer tracer,
        ServiceBusSessionAcquirer.Session session, Duration sessionIdleTimeout, Duration maxSessionLockRenew) {
        this.logger = logger;
        this.sessionId = session.getId();
        this.sessionLink = session.getLink();
        this.hasIdleTimeout = sessionIdleTimeout != null;
        if (hasIdleTimeout) {
            this.disposables
                .add(Flux.switchOnNext(nextItemIdleTimeoutSink.asFlux().map(__ -> Mono.delay(sessionIdleTimeout)))
                    .subscribe(v -> {
                        withLinkInfo(logger.atInfo()).addKeyValue("timeout", sessionIdleTimeout)
                            .log("Did not a receive message within timeout.");
                        terminateEndpointStatesSink.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST);
                    }));
        }
        this.disposables.add(session.beginLockRenew(tracer, maxSessionLockRenew));
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getHostname() {
        return sessionLink.getHostname();
    }

    @Override
    public String getConnectionId() {
        return sessionLink.getConnectionId();
    }

    @Override
    public String getLinkName() {
        return sessionLink.getLinkName();
    }

    @Override
    public String getEntityPath() {
        return sessionLink.getEntityPath();
    }

    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        final Flux<AmqpEndpointState> endpointStates;
        if (hasIdleTimeout) {
            endpointStates = sessionLink.getEndpointStates().takeUntilOther(terminateEndpointStatesSink.asMono());
        } else {
            endpointStates = sessionLink.getEndpointStates();
        }
        return endpointStates.onErrorResume(e -> {
            withLinkInfo(logger.atWarning()).log("Error occurred. Ending session {}.", sessionId, e);
            return Mono.empty();
        });
    }

    @Override
    public Flux<Message> receive() {
        if (hasIdleTimeout) {
            final Mono<Void> beginIdleTimer = Mono.defer(() -> {
                nextItemIdleTimeoutSink.emitNext(true, Sinks.EmitFailureHandler.FAIL_FAST);
                return Mono.empty();
            });
            final Flux<Message> messages = sessionLink.receive();
            return beginIdleTimer.thenMany(messages).doOnNext(m -> {
                nextItemIdleTimeoutSink.emitNext(true, Sinks.EmitFailureHandler.FAIL_FAST);
            });
        } else {
            return sessionLink.receive();
        }
    }

    @Override
    public Mono<Void> updateDisposition(String deliveryTag, DeliveryState deliveryState) {
        return sessionLink.updateDisposition(deliveryTag, deliveryState);
    }

    @Override
    public void addCredit(Supplier<Long> creditSupplier) {
        sessionLink.addCredit(creditSupplier);
    }

    @Override
    public void dispose() {
        disposables.dispose();
        sessionLink.dispose();
    }

    @Override
    public Mono<Void> closeAsync() {
        disposables.dispose();
        return sessionLink.closeAsync();
    }

    @Override
    public Mono<Void> addCredits(int credits) {
        return monoError(logger,
            new UnsupportedOperationException("addCredits(int) should not be called in V2 route."));
    }

    @Override
    public int getCredits() {
        throw logger
            .logExceptionAsError(new UnsupportedOperationException("getCredits() should not be called in V2 route."));
    }

    @Override
    public void setEmptyCreditListener(Supplier<Integer> creditSupplier) {
        throw logger.logExceptionAsError(
            new UnsupportedOperationException("setEmptyCreditListener should not be called in V2 route."));
    }

    private LoggingEventBuilder withLinkInfo(LoggingEventBuilder builder) {
        return builder.addKeyValue(SESSION_ID_KEY, sessionId).addKeyValue(LINK_NAME_KEY, getLinkName());
    }
}
