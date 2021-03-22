// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

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
    private final Disposable subscriptions;
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();
    private final Flux<Message> messagesProcessor;
    private final AmqpRetryOptions retryOptions;
    private final ClientLogger logger = new ClientLogger(ReactorReceiver.class);
    private final Flux<AmqpEndpointState> endpointStates;

    private final AtomicReference<Supplier<Integer>> creditSupplier = new AtomicReference<>();

    protected ReactorReceiver(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
        ReceiveLinkHandler handler, TokenManager tokenManager, ReactorDispatcher dispatcher,
        AmqpRetryOptions retryOptions) {
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

        this.retryOptions = retryOptions;
        this.endpointStates = this.handler.getEndpointStates()
            .map(state -> {
                logger.verbose("connectionId[{}], path[{}], linkName[{}]: State {}", handler.getConnectionId(),
                    entityPath, getLinkName(), state);
                return AmqpEndpointStateUtil.getConnectionState(state);
            })
            .doOnError(error -> {
                final String message = isDisposed.getAndSet(true)
                    ? "This was already disposed. Dropping error."
                    : "Freeing resources due to error.";
                logger.warning("connectionId[{}] entityPath[{}] linkName[{}] {}",
                    handler.getConnectionId(), entityPath, getLinkName(), message, error);

                completeClose();
            })
            .doOnComplete(() -> {
                final String message = isDisposed.getAndSet(true)
                    ? "This was already disposed."
                    : "Freeing resources.";
                logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] {}", handler.getConnectionId(),
                    entityPath, getLinkName(), message);

                completeClose();
            })
            .cache(1);

        //@formatter:off
        this.subscriptions = Disposables.composite(
            this.endpointStates.subscribe(),

            this.tokenManager.getAuthorizationResults().subscribe(
                response -> {
                    logger.verbose("Token refreshed: {}", response);
                    hasAuthorized.set(true);
                }, error -> {
                    //TODO (conniey): Close reactor receiver because we are no longer authorized.
                    logger.info("connectionId[{}], path[{}], linkName[{}] - tokenRenewalFailure[{}]",
                        handler.getConnectionId(), this.entityPath, getLinkName(), error.getMessage());
                    hasAuthorized.set(false);
                }, () -> hasAuthorized.set(false)),

            amqpConnection.getShutdownSignals().subscribe(signal -> {
                logger.verbose("connectionId[{}] linkName[{}]: Shutdown signal received.", handler.getConnectionId(),
                    getLinkName());

                dispose("Connection shutdown.", null).subscribe();
            }));
        //@formatter:on
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
        dispose("Dispose invoked", null).block(retryOptions.getTryTimeout());
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

    /**
     * Disposes of the receiver when an exception is encountered.
     *
     * @param message Message to log.
     * @param errorCondition Error condition associated with close operation.
     */
    Mono<Void> dispose(String message, ErrorCondition errorCondition) {
        if (isDisposed.getAndSet(true)) {
            return isClosedMono.asMono();
        }

        final String condition = errorCondition != null ? errorCondition.toString() : NOT_APPLICABLE;
        logger.verbose("connectionId[{}], path[{}], linkName[{}] errorCondition[{}]: Setting error condition and "
                + "disposing. {}",
            handler.getConnectionId(), entityPath, getLinkName(), condition, message);

        final Runnable closeReceiver = () -> {
            if (receiver.getLocalState() != EndpointState.CLOSED) {
                receiver.close();

                if (receiver.getCondition() == null) {
                    receiver.setCondition(errorCondition);
                }
            }
        };

        return Mono.fromRunnable(() -> {
            try {
                dispatcher.invoke(closeReceiver);
            } catch (IOException e) {
                logger.warning("Could not schedule disposing of receiver on ReactorDispatcher.", e);
                closeReceiver.run();
            }
        }).then(isClosedMono.asMono());
    }

    /**
     * A mono that completes when the sender has completely closed.
     *
     * @return mono that completes when the sender has completely closed.
     */
    Mono<Void> isClosed() {
        return isClosedMono.asMono();
    }

    /**
     * Takes care of disposing of subscriptions, reactor resources after they've been closed.
     */
    private void completeClose() {
        isClosedMono.emitEmpty((signalType, result) -> {
            logger.warning("connectionId[{}], signal[{}], result[{}]. Unable to emit shutdown signal.",
                handler.getConnectionId(), signalType, result);
            return false;
        });

        subscriptions.dispose();

        if (tokenManager != null) {
            tokenManager.close();
        }

        handler.close();
        receiver.free();
    }

    @Override
    public String toString() {
        return String.format("link name: [%s], entity path: [%s]", receiver.getName(), entityPath);
    }
}
