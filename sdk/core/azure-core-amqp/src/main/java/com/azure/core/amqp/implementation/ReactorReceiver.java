// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
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
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * Handles receiving events from Event Hubs service and translating them to proton-j messages.
 */
public class ReactorReceiver implements AmqpReceiveLink, AsyncCloseable, AutoCloseable {
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
        // Delivered messages are not published on another scheduler because we want the settlement method that happens
        // in decodeDelivery to take place and since proton-j is not thread safe, it could end up with hundreds of
        // backed up deliveries waiting to be settled. (Which, consequently, ends up in a FAIL_OVERFLOW error from
        // the handler.
        this.messagesProcessor = this.handler.getDeliveredMessages()
            .flatMap(delivery -> {
                return Mono.create(sink -> {
                    try {
                        this.dispatcher.invoke(() -> {
                            final Message message = decodeDelivery(delivery);
                            final int creditsLeft = receiver.getRemoteCredit();

                            if (creditsLeft > 0) {
                                sink.success(message);
                                return;
                            }

                            final Supplier<Integer> supplier = creditSupplier.get();
                            final Integer credits = supplier.get();

                            if (credits != null && credits > 0) {
                                logger.info("connectionId[{}] linkName[{}] adding credits[{}]",
                                    handler.getConnectionId(), getLinkName(), creditsLeft, credits);
                                receiver.flow(credits);
                            } else {
                                logger.verbose("connectionId[{}] linkName[{}] There are no credits to add.",
                                    handler.getConnectionId(), getLinkName(), creditsLeft, credits);
                            }

                            sink.success(message);
                        });
                    } catch (IOException | RejectedExecutionException e) {
                        sink.error(e);
                    }
                });
            }, 1);

        this.retryOptions = retryOptions;
        this.endpointStates = this.handler.getEndpointStates()
            .map(state -> {
                logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] State {}", handler.getConnectionId(),
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
            this.tokenManager.getAuthorizationResults()
                .onErrorResume(error -> {
                    // When we encounter an error refreshing authorization results, close the receive link.
                    final Mono<Void> operation =
                        closeAsync(String.format(
                            "connectionId[%s] linkName[%s] Token renewal failure. Disposing receive link.",
                            amqpConnection.getId(), getLinkName()),
                            new ErrorCondition(Symbol.getSymbol(AmqpErrorCondition.NOT_ALLOWED.getErrorCondition()),
                                error.getMessage()));

                    return operation.then(Mono.empty());
                }).subscribe(response -> {
                    logger.verbose("connectionId[{}] linkName[{}] response[{}] Token refreshed.",
                        handler.getConnectionId(), getLinkName(), response);
                }, error -> {
                    }, () -> {
                        logger.verbose("connectionId[{}] entityPath[{}] linkName[{}] Authorization completed.",
                            handler.getConnectionId(), entityPath, getLinkName());

                        closeAsync("Authorization completed. Disposing.", null).subscribe();
                    }),

            amqpConnection.getShutdownSignals().flatMap(signal -> {
                logger.verbose("connectionId[{}] linkName[{}] Shutdown signal received.", handler.getConnectionId(),
                    getLinkName());

                return closeAsync("Connection shutdown.", null);
            }).subscribe());
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
    public Mono<Void> addCredits(int credits) {
        if (isDisposed()) {
            return monoError(logger, new IllegalStateException("Cannot add credits to closed link: " + getLinkName()));
        }

        return Mono.create(sink -> {
            try {
                dispatcher.invoke(() -> {
                    receiver.flow(credits);
                    sink.success();
                });
            } catch (IOException e) {
                sink.error(new UncheckedIOException(String.format(
                    "connectionId[%s] linkName[%s] Unable to schedule work to add more credits.",
                    handler.getConnectionId(), getLinkName()), e));
            } catch (RejectedExecutionException e) {
                sink.error(e);
            }
        });
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
        close();
    }

    @Override
    public void close() {
        closeAsync().block(retryOptions.getTryTimeout());
    }

    @Override
    public Mono<Void> closeAsync() {
        return closeAsync("User invoked close operation.", null);
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
    Mono<Void> closeAsync(String message, ErrorCondition errorCondition) {
        if (isDisposed.getAndSet(true)) {
            return isClosedMono.asMono().publishOn(Schedulers.boundedElastic());
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
                logger.warning("connectionId[{}] linkName[{}] IO sink was closed when scheduling work. Manually "
                        + "invoking and completing close.", handler.getConnectionId(), getLinkName(), e);

                closeReceiver.run();
                completeClose();
            } catch (RejectedExecutionException e) {
                // Not logging error here again because we have to log the exception when we throw it.
                logger.info("connectionId[{}] linkName[{}] RejectedExecutionException when scheduling on "
                        + "ReactorDispatcher. Manually invoking and completing close.", handler.getConnectionId(),
                    getLinkName());

                closeReceiver.run();
                completeClose();
            }
        }).then(isClosedMono.asMono()).publishOn(Schedulers.boundedElastic());
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
        return String.format("connectionId: [%s] entity path: [%s] linkName: [%s]", receiver.getName(), entityPath,
            getLinkName());
    }
}
