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

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
    private final ReactorDispatcher dispatcher;
    private final Disposable.Composite subscriptions;
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final EmitterProcessor<Message> messagesProcessor;
    private final ClientLogger logger = new ClientLogger(ReactorReceiver.class);
    private final ReplayProcessor<AmqpEndpointState> endpointStates =
        ReplayProcessor.cacheLastOrDefault(AmqpEndpointState.UNINITIALIZED);
    private FluxSink<AmqpEndpointState> endpointStateSink = endpointStates.sink(FluxSink.OverflowStrategy.BUFFER);

    private final AtomicReference<Supplier<Integer>> creditSupplier = new AtomicReference<>();

    protected ReactorReceiver(String entityPath, Receiver receiver, ReceiveLinkHandler handler,
        TokenManager tokenManager, ReactorDispatcher dispatcher) {
        this.entityPath = entityPath;
        this.receiver = receiver;
        this.handler = handler;
        this.tokenManager = tokenManager;
        this.dispatcher = dispatcher;
        this.messagesProcessor = this.handler.getDeliveredMessages()
            .map(this::decodeDelivery)
            .doOnNext(next -> {
                if (receiver.getRemoteCredit() == 0 && !isDisposed.get()) {
                    final Supplier<Integer> supplier = creditSupplier.get();
                    if (supplier == null) {
                        return;
                    }

                    final Integer credits = supplier.get();
                    if (credits != null && credits > 0) {
                        addCredits(credits);
                    }
                }
            })
            .subscribeWith(EmitterProcessor.create());

        this.subscriptions = Disposables.composite(
            this.handler.getEndpointStates().subscribe(
                state -> {
                    logger.verbose("Connection state: {}", state);
                    endpointStateSink.next(AmqpEndpointStateUtil.getConnectionState(state));
                }, error -> {
                    logger.error("connectionId[{}] linkName[{}] entityPath[{}] Error occurred in connection.",
                        handler.getConnectionId(), receiver.getName(), entityPath, error);
                    endpointStateSink.error(error);
                    dispose();
                }, () -> {
                    endpointStateSink.next(AmqpEndpointState.CLOSED);
                    dispose();
                }),

            this.handler.getErrors().subscribe(error -> {
                logger.error("connectionId[{}] linkName[{}] entityPath[{}] Error occurred in link.",
                    handler.getConnectionId(), receiver.getName(), entityPath, error);
                endpointStateSink.error(error);
                dispose();
            }),

            this.tokenManager.getAuthorizationResults().subscribe(
                response -> {
                    logger.verbose("Token refreshed: {}", response);
                    hasAuthorized.set(true);
                }, error -> {
                    logger.info("connectionId[{}], path[{}], linkName[{}] - tokenRenewalFailure[{}]",
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
        if (!isDisposed.get()) {
            try {
                dispatcher.invoke(() -> receiver.flow(credits));
            } catch (IOException e) {
                logger.warning("Unable to schedule work to add more credits.", e);
            }
        }
    }

    @Override
    public int getCredits() {
        return receiver.getRemoteCredit();
    }

    @Override
    public void setEmptyCreditListener(Supplier<Integer> creditSupplier) {
        Objects.requireNonNull(creditSupplier, "'creditSupplier' cannot be null.");
        this.creditSupplier.set(creditSupplier);
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
    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        subscriptions.dispose();
        endpointStateSink.complete();
        messagesProcessor.onComplete();
        tokenManager.close();
        receiver.close();

        try {
            dispatcher.invoke(() -> {
                receiver.free();
                handler.close();
            });
        } catch (IOException e) {
            logger.warning("Could not schedule disposing of receiver on ReactorDispatcher.", e);
            handler.close();
        }
    }

    protected Message decodeDelivery(Delivery delivery) {
        final int messageSize = delivery.pending();
        final byte[] buffer = new byte[messageSize];
        final int read = receiver.recv(buffer, 0, messageSize);
        receiver.advance();

        final Message message = Proton.message();
        message.decode(buffer, 0, read);

        delivery.settle();
        return message;
    }

    @Override
    public String toString() {
        return String.format("link name: [%s], entity path: [%s]", receiver.getName(), entityPath);
    }
}
