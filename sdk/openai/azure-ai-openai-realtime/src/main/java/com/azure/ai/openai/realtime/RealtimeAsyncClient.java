// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.LoggingUtils;
import com.azure.ai.openai.realtime.implementation.websocket.AuthenticationProvider;
import com.azure.ai.openai.realtime.implementation.websocket.ClientEndpointConfiguration;
import com.azure.ai.openai.realtime.implementation.websocket.CloseReason;
import com.azure.ai.openai.realtime.implementation.websocket.RealtimeClientState;
import com.azure.ai.openai.realtime.implementation.websocket.WebSocketClient;
import com.azure.ai.openai.realtime.implementation.websocket.WebSocketClientNettyImpl;
import com.azure.ai.openai.realtime.implementation.websocket.WebSocketSession;
import com.azure.ai.openai.realtime.models.RealtimeClientEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.ai.openai.realtime.models.RealtimeServerEventError;
import com.azure.ai.openai.realtime.models.SendMessageFailedException;
import com.azure.ai.openai.realtime.models.ServerErrorReceivedException;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Initializes a new instance of the asynchronous RealtimeClient type.
 */
@ServiceClient(builder = RealtimeClientBuilder.class, isAsync = true)
public final class RealtimeAsyncClient implements Closeable {

    // logging
    private final ClientLogger logger;
    private final AtomicReference<ClientLogger> loggerReference = new AtomicReference<>();
    // authentication
    private final AuthenticationProvider authenticationProvider;
    // client state
    private final ClientState clientState = new ClientState();
    // state on close
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();
    // state on stop by user
    private final AtomicBoolean isStoppedByUser = new AtomicBoolean();
    private final AtomicReference<Sinks.Empty<Void>> isStoppedByUserSink = new AtomicReference<>();

    // websocket client
    private final WebSocketClient webSocketClient;
    private final AtomicReference<WebSocketSession> webSocketSession = new AtomicReference<>();

    // client specifics
    private final ClientEndpointConfiguration clientEndpointConfiguration;
    private final String applicationId;

    private static final Duration CLOSE_AFTER_SESSION_OPEN_DELAY = Duration.ofMillis(100);

    // incoming message handlers:

    // Server catch all:
    private final AtomicReference<Sinks.Many<RealtimeServerEvent>> serverEvents
        = new AtomicReference<>(Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false));

    /**
     * Creates a RealtimeAsyncClient.
     *
     * @param cec The client endpoint configuration containing base URL, headers and other information.
     * @param applicationId The application ID.
     * @param authenticationProvider The authentication provider. Currently only supports KeyCredential and TokenCredential
     */
    RealtimeAsyncClient(ClientEndpointConfiguration cec, String applicationId,
        AuthenticationProvider authenticationProvider) {
        this.logger
            = new ClientLogger(RealtimeAsyncClient.class, LoggingUtils.createContextWithApplicationId(applicationId));
        loggerReference.set(logger);

        this.webSocketClient = new WebSocketClientNettyImpl();
        this.clientEndpointConfiguration = cec;
        this.applicationId = applicationId;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * Closes the client.
     */
    @Override
    public void close() {
        if (this.isDisposed.getAndSet(true)) {
            this.isClosedMono.asMono().block();
        } else {
            stop().then(Mono.fromRunnable(() -> {
                this.clientState.changeState(RealtimeClientState.CLOSED);

                isClosedMono.emitEmpty(emitFailureHandler("Unable to emit Close"));
            })).block();
        }
    }

    /**
     * Starts the client for connecting to the server.
     *
     * @return the task.
     */
    public Mono<Void> start() {
        return this.start(null);
    }

    /**
     * Stops the client and disconnects from the server.
     *
     * @return the task.
     */
    public Mono<Void> stop() {
        if (clientState.get() == RealtimeClientState.CLOSED) {
            return Mono
                .error(logger.logExceptionAsError(new IllegalStateException("Failed to stop. Client is CLOSED.")));
        }

        return Mono.defer(this::doStop);
    }

    /**
     * Sends a message to the server.
     *
     * @param message client message to send.
     * @return the task.
     */
    public Mono<Void> sendMessage(RealtimeClientEvent message) {
        return checkStateBeforeSend().then(Mono.create(sink -> {
            WebSocketSession localWebSocketSession = this.webSocketSession.get();
            if (localWebSocketSession != null) {
                localWebSocketSession.sendObjectAsync(message, sendResult -> {
                    if (sendResult.isOK()) {
                        sink.success();
                    } else {
                        sink.error(logSendMessageFailedException("Failed to send message.", sendResult.getException(),
                            true, message));
                    }
                });
            } else {
                sink.error(logSendMessageFailedException("Failed to send message. Websocket session is null.", null,
                    false, message));
            }

        }));
    }

    /**
     * Returns a flux of server events.
     *
     * @return the server events.
     */
    public Flux<RealtimeServerEvent> getServerEvents() {
        Sinks.Many<RealtimeServerEvent> localServerEvents = serverEvents.get();
        return localServerEvents.asFlux().transform(serverEvents -> serverEvents.flatMap(event -> {
            if (event instanceof RealtimeServerEventError) {
                RealtimeServerEventError errorEvent = (RealtimeServerEventError) event;
                logger.atError()
                    .addKeyValue("RealtimeServerErrorEvent", event.getEventId())
                    .log(errorEvent.getError().getMessage());
                return Flux.error(ServerErrorReceivedException.fromRealtimeServerEventError(errorEvent));
            } else {
                return Flux.just(event);
            }
        }));
    }

    Mono<Void> start(Runnable postStartTask) {
        if (clientState.get() == RealtimeClientState.CLOSED) {
            return Mono
                .error(logger.logExceptionAsError(new IllegalStateException("Failed to start. Client is CLOSED.")));
        }

        return Mono.defer(() -> {
            logger.atInfo().addKeyValue("currentClientState", clientState.get()).log("Start client called.");

            isStoppedByUser.set(false);
            isStoppedByUserSink.set(null);

            boolean success = clientState.changeStateOn(RealtimeClientState.STOPPED, RealtimeClientState.CONNECTING);
            if (!success) {
                return Mono.error(
                    logger.logExceptionAsError(new IllegalStateException("Failed to start. Client is not STOPPED.")));
            } else {
                if (postStartTask != null) {
                    postStartTask.run();
                }

                return Mono.empty();
            }
        })
            .then(authenticationProvider.authenticationToken())
            .flatMap(authenticationHeader -> Mono.<Void>fromRunnable(() -> {
                this.webSocketSession
                    .set(webSocketClient.connectToServer(this.clientEndpointConfiguration, () -> authenticationHeader,
                        this::handleMessage, this::handleSessionOpen, this::handleSessionClose));
            }))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnError(error -> {
                // stop if error, do not send StoppedEvent when it fails at start(), which would have exception thrown
                handleClientStop(false);
            });
    }

    private Mono<Void> doStop() {
        logger.atInfo().addKeyValue("currentClientState", clientState.get()).log("Stop client called.");

        if (clientState.get() == RealtimeClientState.STOPPED) {
            // already STOPPED
            return Mono.empty();
        } else if (clientState.get() == RealtimeClientState.STOPPING) {
            // already STOPPING
            // isStoppedByUserMono will be signaled in handleClientStop
            return getStoppedByUserMono();
        }

        // reset
        isStoppedByUser.compareAndSet(false, true);

        WebSocketSession localSession = this.webSocketSession.get();
        if (localSession != null && localSession.isOpen()) {
            // should be CONNECTED
            clientState.changeState(RealtimeClientState.STOPPING);
            return Mono.fromCallable(() -> {
                localSession.close();
                return (Void) null;
            }).subscribeOn(Schedulers.boundedElastic());
        } else {
            if (clientState.changeStateOn(RealtimeClientState.DISCONNECTED, RealtimeClientState.STOPPED)) {
                // handle transient state DISCONNECTED, directly change to STOPPED,
                // RECONNECTING via handleNoRecovery when autoReconnect=true
                handleClientStop();
                return Mono.empty();
            } else {
                // handle transient state e.g. CONNECTING, RECOVERING, RECONNECTING
                // handleSessionOpen will close session if isStoppedByUser=true
                // isStoppedByUserMono will be signaled in handleClientStop
                return getStoppedByUserMono();
            }
        }
    }

    private Mono<Void> getStoppedByUserMono() {
        Sinks.Empty<Void> sink = Sinks.empty();
        boolean isStoppedByUserMonoSet = isStoppedByUserSink.compareAndSet(null, sink);
        if (!isStoppedByUserMonoSet) {
            sink = isStoppedByUserSink.get();
        }
        return sink == null ? Mono.empty() : sink.asMono();
    }

    private Mono<Void> checkStateBeforeSend() {
        return Mono.defer(() -> {
            RealtimeClientState state = clientState.get();
            if (state == RealtimeClientState.CLOSED) {
                return Mono.error(logger.logExceptionAsError(
                    new IllegalStateException("Failed to send message. RealtimeAsyncClient is CLOSED.")));
            }
            if (state != RealtimeClientState.CONNECTED) {
                return Mono.error(
                    logSendMessageFailedException("Failed to send message. Client is " + state.name() + ".", null,
                        state == RealtimeClientState.RECOVERING
                            || state == RealtimeClientState.CONNECTING
                            || state == RealtimeClientState.RECONNECTING
                            || state == RealtimeClientState.DISCONNECTED,
                        (String) null));
            }

            WebSocketSession localWebSocketSession = this.webSocketSession.get();
            if (localWebSocketSession == null || !localWebSocketSession.isOpen()) {
                // something unexpected
                return Mono.error(logSendMessageFailedException(
                    "Failed to send message. Websocket session is not opened.", null, false, (String) null));
            } else {
                return Mono.empty();
            }
        });
    }

    private void handleMessage(Object message) {
        Sinks.Many<RealtimeServerEvent> localServerEvents = serverEvents.get();
        if (localServerEvents == null) {
            logger.atError().log(() -> "Server event sink is null");
        } else {
            localServerEvents.tryEmitNext((RealtimeServerEvent) message);
        }
    }

    private void handleSessionOpen(WebSocketSession session) {
        logger.atVerbose().log(() -> "Session opened");

        clientState.changeState(RealtimeClientState.CONNECTED);
        if (isStoppedByUser.compareAndSet(true, false)) {
            // user intended to stop, but issued when session is not CONNECTED or STOPPED,
            // e.g. CONNECTING, RECOVERING, RECONNECTING

            // delay a bit, as handleSessionOpen is in websocket callback
            Mono.delay(CLOSE_AFTER_SESSION_OPEN_DELAY).then(Mono.fromCallable(() -> {
                clientState.changeState(RealtimeClientState.STOPPING);

                if (session != null && session.isOpen()) {
                    session.close();
                } else {
                    logger.atError().log("Failed to close session after session open");
                    handleClientStop();
                }
                return (Void) null;
            }).subscribeOn(Schedulers.boundedElastic())).subscribe(null, thr -> {
                logger.atError().log("Failed to close session after session open: " + thr.getMessage());
                // force a stopped state
                handleClientStop();
            });
        }
    }

    private void handleSessionClose(CloseReason closeReason) {
        logger.atVerbose().addKeyValue("code", closeReason.getCloseCode()).log(() -> "Session closed");

        final int violatedPolicyStatusCode = 1008;

        if (clientState.get() == RealtimeClientState.STOPPED) {
            return;
        }

        if (isStoppedByUser.compareAndSet(true, false) || clientState.get() == RealtimeClientState.STOPPING) {

            // stopped by user
            handleClientStop();
        } else if (closeReason.getCloseCode() == violatedPolicyStatusCode) {
            clientState.changeState(RealtimeClientState.DISCONNECTED);
        }
    }

    private void handleClientStop() {
        handleClientStop(true);
    }

    private void handleClientStop(boolean sendStoppedEvent) {
        clientState.changeState(RealtimeClientState.STOPPED);
        // session
        this.webSocketSession.set(null);

        tryCompleteOnStoppedByUserSink();

        Sinks.Many<RealtimeServerEvent> localServerEvents = serverEvents.get();

        if (localServerEvents != null) {
            localServerEvents.emitComplete(emitFailureHandler("Unable to emit Complete to serverEvents"));
        }
        serverEvents.set(Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false));

        // Close and re-initialize any additional sinks we may add here in the future
    }

    private void tryCompleteOnStoppedByUserSink() {
        // clear isStoppedByUserMono
        Sinks.Empty<Void> mono = isStoppedByUserSink.getAndSet(null);
        if (mono != null) {
            mono.emitEmpty(emitFailureHandler("Unable to emit Stopped"));
        }
    }

    private Sinks.EmitFailureHandler emitFailureHandler(String message) {
        return (signalType, emitResult) -> {
            LoggingUtils.addSignalTypeAndResult(this.logger.atWarning(), signalType, emitResult).log(message);
            return emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED);
        };
    }

    private final class ClientState {
        private final AtomicReference<RealtimeClientState> clientState
            = new AtomicReference<>(RealtimeClientState.STOPPED);

        RealtimeClientState get() {
            return clientState.get();
        }

        RealtimeClientState changeState(RealtimeClientState newState) {
            RealtimeClientState previousState = clientState.getAndSet(newState);
            logger.atInfo()
                .addKeyValue("currentClientState", newState)
                .addKeyValue("previousClientState", previousState)
                .log("Client state changed");
            return previousState;
        }

        boolean changeStateOn(RealtimeClientState expectedCurrentState, RealtimeClientState newState) {
            boolean success = clientState.compareAndSet(expectedCurrentState, newState);
            if (success) {
                logger.atInfo()
                    .addKeyValue("currentClientState", newState)
                    .addKeyValue("previousClientState", expectedCurrentState)
                    .log("Client state changed.");
            }
            return success;
        }
    }

    private RuntimeException logSendMessageFailedException(String errorMessage, Throwable cause, boolean isTransient,
        RealtimeClientEvent message) {
        return logSendMessageFailedException(errorMessage, cause, isTransient, message.getEventId());
    }

    private RuntimeException logSendMessageFailedException(String errorMessage, Throwable cause, boolean isTransient,
        String eventId) {

        return logSendMessageFailedException(errorMessage, cause, isTransient, eventId, null);
    }

    private RuntimeException logSendMessageFailedException(String errorMessage, Throwable cause, boolean isTransient,
        String eventId, RealtimeServerEventError error) {
        return logger
            .logExceptionAsWarning(new SendMessageFailedException(errorMessage, cause, isTransient, eventId, error));
    }
}
