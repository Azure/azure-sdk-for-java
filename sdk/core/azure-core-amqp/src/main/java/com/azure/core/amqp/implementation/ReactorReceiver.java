// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Handles receiving events from Event Hubs service and translating them to proton-j messages.
 */
public class ReactorReceiver implements AmqpReceiveLink {
    private final String entityPath;
    private final Receiver receiver;
    private final ReceiveLinkHandler handler;
    private final TokenManager tokenManager;
    private final ReactorDispatcher dispatcher;
    private final Disposable subscriptions;
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Flux<Message> messagesProcessor;
    private final ClientLogger logger = new ClientLogger(ReactorReceiver.class);
    private final Flux<AmqpEndpointState> endpointStates;

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
            .publish()
            .autoConnect();
        this.endpointStates = this.handler.getEndpointStates()
            .map(state -> {
                logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] State {}", handler.getConnectionId(),
                    entityPath, getLinkName(), state);
                return AmqpEndpointStateUtil.getConnectionState(state);
            })
            .cache(1);

        this.subscriptions = this.tokenManager.getAuthorizationResults().subscribe(
            response -> {
                logger.verbose("connectionId[{}] linkName[{}] Token refreshed. {}", handler.getConnectionId(),
                    getLinkName(), response);
            }, error -> {
                logger.info("connectionId[{}] entityPath{}] linkName[{}] tokenRenewalFailure[{}]",
                    handler.getConnectionId(), this.entityPath, getLinkName(), error.getMessage());
                dispose(new ErrorCondition(Symbol.getSymbol(AmqpErrorCondition.NOT_ALLOWED.getErrorCondition()),
                    error.getMessage()));
            }, () -> {
                logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] Authorization completed. Disposing.",
                    handler.getConnectionId(), entityPath, getLinkName());
                dispose();
            });
    }

    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates.distinct();
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
    public void addCreditsInstantly(int credits) {
        receiver.flow(credits);
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
        return handler.getLinkName();
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
        tokenManager.close();
        receiver.close();

        try {
            dispatcher.invoke(() -> {
                receiver.free();
                handler.close();
            });
        } catch (IOException e) {
            logger.warning("connectionId[{}] linkName[{}] Could not schedule disposing of receiver.",
                handler.getConnectionId(), getLinkName(), e);
            receiver.free();
            handler.close();
        }
    }

    /**
     * Disposes of the receiver when an exception is encountered.
     *
     * @param condition Error condition associated with close operation.
     */
    void dispose(ErrorCondition condition) {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] Setting error condition {}",
            handler.getConnectionId(), entityPath, getLinkName(), condition);

        if (receiver.getLocalState() != EndpointState.CLOSED) {
            receiver.close();

            if (receiver.getCondition() == null) {
                receiver.setCondition(condition);
            }
        }

        try {
            dispatcher.invoke(() -> {
                receiver.free();
                handler.close();
            });
        } catch (IOException e) {
            logger.warning("connectionId[{}] entityPath[{}] linkName[{}] Could not schedule disposing of receiver "
                + "on dispatcher. Manually invoking.", handler.getConnectionId(), entityPath, getLinkName(), e);

            receiver.free();
            handler.close();
        }

        tokenManager.close();
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
        return String.format("connectionId: [%s] entity path: [%s] linkName: [%s]", receiver.getName(), entityPath,
            getLinkName());
    }
}
