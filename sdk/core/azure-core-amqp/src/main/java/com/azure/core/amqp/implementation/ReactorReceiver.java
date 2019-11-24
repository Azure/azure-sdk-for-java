// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Handles receiving events from Event Hubs service and translating them to proton-j messages.
 */
public class ReactorReceiver implements AmqpReceiveLink {
    // Initial value is true because we could not have created this receiver without authorising against the CBS node
    // first.
    private final AtomicBoolean hasAuthorized = new AtomicBoolean(true);

    private final String entityPath;
    private final Receiver receiver;
    private final ReceiveLinkHandler handler;
    private final TokenManager tokenManager;
    private final Disposable.Composite subscriptions;
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final EmitterProcessor<Message> messagesProcessor = EmitterProcessor.create();
    private FluxSink<Message> messageSink = messagesProcessor.sink();
    private final ClientLogger logger = new ClientLogger(ReactorReceiver.class);
    private final ReplayProcessor<AmqpEndpointState> endpointStates =
        ReplayProcessor.cacheLastOrDefault(AmqpEndpointState.UNINITIALIZED);
    private FluxSink<AmqpEndpointState> endpointStateSink = endpointStates.sink(FluxSink.OverflowStrategy.BUFFER);

    private volatile Supplier<Integer> creditSupplier;

    ReactorReceiver(String entityPath, Receiver receiver, ReceiveLinkHandler handler, TokenManager tokenManager) {
        this.entityPath = entityPath;
        this.receiver = receiver;
        this.handler = handler;
        this.tokenManager = tokenManager;

        this.subscriptions = Disposables.composite(
            this.handler.getDeliveredMessages().subscribe(this::decodeDelivery),

            this.handler.getEndpointStates().subscribe(
                state -> {
                    logger.verbose("Connection state: {}", state);
                    endpointStateSink.next(AmqpEndpointStateUtil.getConnectionState(state));
                }, error -> {
                    logger.error("Error occurred in connection.", error);
                    endpointStateSink.error(error);
                    close();
                }, () -> {
                    endpointStateSink.next(AmqpEndpointState.CLOSED);
                    close();
                }),

            this.handler.getErrors().subscribe(error -> {
                logger.error("Error occurred in link.", error);
                endpointStateSink.error(error);
                close();
            }),

            this.tokenManager.getAuthorizationResults().subscribe(
                response -> {
                    logger.verbose("Token refreshed: {}", response);
                    hasAuthorized.set(true);
                }, error -> {
                    logger.info("clientId[{}], path[{}], linkName[{}] - tokenRenewalFailure[{}]",
                        handler.getConnectionId(), this.entityPath, getLinkName(), error.getMessage());
                    hasAuthorized.set(false);
                }, () -> hasAuthorized.set(false)));
    }

    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates;
    }

    @Override
    public Flux<Message> receive() {
        return messagesProcessor;
    }

    @Override
    public void addCredits(int credits) {
        receiver.flow(credits);
    }

    @Override
    public int getCredits() {
        return receiver.getRemoteCredit();
    }

    @Override
    public void setEmptyCreditListener(Supplier<Integer> creditSupplier) {
        Objects.requireNonNull(creditSupplier);
        this.creditSupplier = creditSupplier;
    }

    @Override
    public String getLinkName() {
        return receiver.getName();
    }

    @Override
    public String getEntityPath() {
        return entityPath;
    }

    @Override
    public String getHostname() {
        return handler.getHostname();
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        subscriptions.dispose();
        endpointStateSink.complete();
        messageSink.complete();
        tokenManager.close();
        handler.close();
    }

    private void decodeDelivery(Delivery delivery) {
        final int messageSize = delivery.pending();
        final byte[] buffer = new byte[messageSize];
        final int read = receiver.recv(buffer, 0, messageSize);
        receiver.advance();

        final Message message = Proton.message();
        message.decode(buffer, 0, read);

        delivery.settle();

        messageSink.next(message);

        if (receiver.getRemoteCredit() == 0 && creditSupplier != null) {
            final Integer credits = creditSupplier.get();

            if (credits != null && credits > 0) {
                addCredits(credits);
            }
        }
    }
}
