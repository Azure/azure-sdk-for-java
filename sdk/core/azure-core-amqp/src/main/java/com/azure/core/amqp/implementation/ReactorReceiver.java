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
import com.azure.core.util.metrics.AzureMeter;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addErrorCondition;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addSignalTypeAndResult;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
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
    // Indicates if this ReactorReceiver is disposed or disposal is in progress.
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    // A Mono that signals completion when the disposal/closing of ReactorReceiver is completed.
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();
    // Indicate if the completeClose method is called.
    private final AtomicBoolean isCompleteCloseCalled = new AtomicBoolean();
    private final Flux<Message> messagesProcessor;
    private final AmqpRetryOptions retryOptions;
    private final ClientLogger logger;
    private final Flux<AmqpEndpointState> endpointStates;
    private final Sinks.Empty<AmqpEndpointState> terminateEndpointStates = Sinks.empty();
    private final AzureMeter meter;

    private final AtomicReference<Supplier<Integer>> creditSupplier = new AtomicReference<>();

    protected ReactorReceiver(AmqpConnection amqpConnection, String entityPath, Receiver receiver,
                              ReceiveLinkHandler handler, TokenManager tokenManager, ReactorDispatcher dispatcher,
                              AmqpRetryOptions retryOptions, AzureMeter meter) {
        this.entityPath = entityPath;
        this.receiver = receiver;
        this.handler = handler;
        this.tokenManager = tokenManager;
        this.dispatcher = dispatcher;
        this.meter = meter;

        Map<String, Object> loggingContext = createContextWithConnectionId(handler.getConnectionId());
        loggingContext.put(LINK_NAME_KEY, this.handler.getLinkName());
        loggingContext.put(ENTITY_PATH_KEY, entityPath);

        this.logger = new ClientLogger(ReactorReceiver.class, loggingContext);

        // Delivered messages are not published on another scheduler because we want the settlement method that happens
        // in decodeDelivery to take place and since proton-j is not thread safe, it could end up with hundreds of
        // backed up deliveries waiting to be settled. (Which, consequently, ends up in a FAIL_OVERFLOW error from
        // the handler.
        this.messagesProcessor = this.handler.getDeliveredMessages()
            .flatMap(delivery -> {
                return Mono.create(sink -> {
                    try {
                        this.dispatcher.invoke(() -> {
                            if (isDisposed()) {
                                sink.error(new IllegalStateException(
                                    "Cannot decode delivery when ReactorReceiver instance is closed."));
                                return;
                            }
                            final Message message = decodeDelivery(delivery);
                            final int creditsLeft = receiver.getRemoteCredit();

                            if (creditsLeft > 0) {
                                sink.success(message);
                                return;
                            }

                            final Supplier<Integer> supplier = creditSupplier.get();
                            final Integer credits = supplier.get();

                            if (credits != null && credits > 0) {
                                logger.atInfo()
                                    .addKeyValue("credits", credits)
                                    .log("Adding credits.");
                                receiver.flow(credits);
                            } else {
                                logger.atVerbose()
                                    .addKeyValue("credits", credits)
                                    .log("There are no credits to add.");
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
                logger.atVerbose()
                    .log("State {}", state);
                return AmqpEndpointStateUtil.getConnectionState(state);
            })
            .doOnError(error -> {
                final String message = isDisposed.getAndSet(true)
                    ? "This was already disposed. Dropping error."
                    : "Freeing resources due to error.";

                logger.atInfo()
                    .log(message);

                completeClose();
            })
            .doOnComplete(() -> {
                final String message = isDisposed.getAndSet(true)
                    ? "This was already disposed."
                    : "Freeing resources.";

                logger.atVerbose()
                    .log(message);

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
                        closeAsync("Token renewal failure. Disposing receive link.",
                            new ErrorCondition(Symbol.getSymbol(AmqpErrorCondition.NOT_ALLOWED.getErrorCondition()),
                                error.getMessage()));

                    return operation.then(Mono.empty());
                }).subscribe(response ->
                    logger.atVerbose()
                        .addKeyValue("response", response)
                        .log("Token refreshed."),
                    error -> { },
                    () -> {
                        logger.atVerbose()
                            .log("Authorization completed.");

                        closeAsync("Authorization completed. Disposing.", null).subscribe();
                    }),

            amqpConnection.getShutdownSignals().flatMap(signal -> {
                logger.verbose("Shutdown signal received.");
                return closeAsync("Connection shutdown.", null);
            }).subscribe());
        //@formatter:on
    }

    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates
            .distinctUntilChanged()
            .takeUntilOther(this.terminateEndpointStates.asMono());
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
     * <p>
     * While {@link ReactorReceiver#closeAsync()} exposes disposal API through {@link AsyncCloseable}
     * contract, this API performs the same disposal with additional
     * contextual information. For example, the context may indicate if the resource needs to be disposed of
     * internally when there is an error in the link, session or connection.
     * </p>
     *
     * <p>
     * Closing ReactorReceiver involves 3 stages, running in following order  -
     * <ul>
     *      <li>local-close (client to broker) via beginClose() </li>
     *      <li>remote-close ack (broker to client)</li>
     *      <li>disposal of ReactorReceiver resources via completeClose()</li>
     * </ul>
     * @link <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-amqp/docs/reactor-receiver-closeflow.png">Reactor receiver close flow</a>
     *
     * @param message Message to log.
     * @param errorCondition Error condition associated with close operation.
     */
    protected Mono<Void> closeAsync(String message, ErrorCondition errorCondition) {
        if (isDisposed.getAndSet(true)) {
            return getIsClosedMono();
        }

        addErrorCondition(logger.atVerbose(), errorCondition)
            .log("Setting error condition and disposing. {}", message);

        return beginClose(errorCondition)
            .flatMap(localCloseScheduled -> {
                if (localCloseScheduled) {
                    return timeoutRemoteCloseAck();
                } else {
                    return Mono.empty();
                }
            })
            .publishOn(Schedulers.boundedElastic());
    }

    /**
     * Gets the Mono that signals completion when the disposal/closing of ReactorReceiver is completed.
     *
     * @return the disposal/closing completion Mono.
     */
    protected Mono<Void> getIsClosedMono() {
        return isClosedMono.asMono().publishOn(Schedulers.boundedElastic());
    }

    /**
     * Beings the client side close by initiating local-close on underlying receiver.
     *
     * @param errorCondition Error condition associated with close operation.
     * @return a {@link Mono} when subscribed attempt to initiate local-close, emitting {@code true}
     *     if local-close is scheduled on the dispatcher, emits {@code false} if unable to schedule
     *     local-close that lead to manual close.
     */
    private Mono<Boolean> beginClose(ErrorCondition errorCondition) {
        final Runnable localClose = () -> {
            if (receiver.getLocalState() != EndpointState.CLOSED) {
                receiver.close();

                if (receiver.getCondition() == null) {
                    receiver.setCondition(errorCondition);
                }
            }
        };

        return Mono.create(sink -> {
            boolean localCloseScheduled = false;
            try {
                dispatcher.invoke(localClose);
                localCloseScheduled = true;
            } catch (IOException e) {
                logger.warning("IO sink was closed when scheduling work. Manually invoking and completing close.", e);

                localClose.run();
                terminateEndpointState();
                completeClose();
            } catch (RejectedExecutionException e) {
                // Not logging error here again because we have to log the exception when we throw it.
                logger.info("RejectedExecutionException when scheduling on ReactorDispatcher. Manually invoking and completing close.");

                localClose.run();
                terminateEndpointState();
                completeClose();
            } finally {
                sink.success(localCloseScheduled);
            }
        });
    }

    /**
     * Apply timeout on remote-close ack. If timeout happens, i.e., if remote-close ack doesn't arrive within
     * the timeout duration, then terminate the Flux returned by getEndpointStates() and complete close.
     *
     * a {@link Mono} that registers remote-close ack timeout based close cleanup.
     */
    private Mono<Void> timeoutRemoteCloseAck() {
        return isClosedMono.asMono()
            .timeout(retryOptions.getTryTimeout())
            .onErrorResume(error -> {
                if (error instanceof TimeoutException) {
                    logger.info("Timeout waiting for RemoteClose. Manually terminating EndpointStates and completing close.");
                    terminateEndpointState();
                    completeClose();
                }
                return Mono.empty();
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Terminate the Flux returned by the getEndpointStates() API.
     *
     * <p>
     * The termination of Flux returned by getEndpointStates() is the signal that "AmqpReceiveLinkProcessor"
     * uses to either terminate its downstream or obtain a new ReactorReceiver to continue delivering events
     * downstream.
     * </p>
     */
    private void terminateEndpointState() {
        this.terminateEndpointStates.emitEmpty((signalType, emitResult) -> {
            addSignalTypeAndResult(logger.atVerbose(), signalType, emitResult)
                .log("Could not emit EndpointStates termination.");
            return false;
        });
    }

    /**
     * Completes the closing of the underlying receiver, which includes disposing of subscriptions,
     * closing of token manager, and releasing of protonJ resources.
     * <p>
     * The completeClose invoked in 3 cases - when the broker ack for beginClose (i.e. ack via
     * remote-close frame), if the broker ack for beginClose never comes through within timeout,
     * if the client fails to run beginClose.
     * </p>
     */
    private void completeClose() {
        if (isCompleteCloseCalled.getAndSet(true)) {
            return;
        }

        isClosedMono.emitEmpty((signalType, result) -> {
            addSignalTypeAndResult(logger.atWarning(), signalType, result)
                .log("Unable to emit shutdown signal.");
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
