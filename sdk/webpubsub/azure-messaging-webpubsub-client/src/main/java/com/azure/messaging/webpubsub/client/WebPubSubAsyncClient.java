// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubConnection;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketClient;
import com.azure.messaging.webpubsub.client.implementation.websocket.ClientEndpointConfiguration;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketClientNettyImpl;
import com.azure.messaging.webpubsub.client.implementation.websocket.CloseReason;
import com.azure.messaging.webpubsub.client.implementation.websocket.WebSocketSession;
import com.azure.messaging.webpubsub.client.models.RejoinGroupFailedEvent;
import com.azure.messaging.webpubsub.client.models.SendMessageFailedException;
import com.azure.messaging.webpubsub.client.implementation.models.AckMessage;
import com.azure.messaging.webpubsub.client.implementation.models.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.LoggingUtils;
import com.azure.messaging.webpubsub.client.implementation.models.SendEventMessage;
import com.azure.messaging.webpubsub.client.implementation.models.SequenceAckMessage;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubGroup;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessageAck;
import com.azure.messaging.webpubsub.client.models.AckResponseError;
import com.azure.messaging.webpubsub.client.implementation.models.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.JoinGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.models.LeaveGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.models.SendToGroupMessage;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import com.azure.messaging.webpubsub.client.models.DisconnectedEvent;
import com.azure.messaging.webpubsub.client.implementation.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.models.GroupMessageEvent;
import com.azure.messaging.webpubsub.client.models.SendEventOptions;
import com.azure.messaging.webpubsub.client.models.SendToGroupOptions;
import com.azure.messaging.webpubsub.client.implementation.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.models.ServerMessageEvent;
import com.azure.messaging.webpubsub.client.models.StoppedEvent;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubProtocolType;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;
import reactor.util.retry.Retry;

import java.io.Closeable;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The WebPubSubAsync client.
 */
final class WebPubSubAsyncClient implements Closeable {

    // logging
    private ClientLogger logger;
    private final AtomicReference<ClientLogger> loggerReference = new AtomicReference<>();

    // options
    private final Mono<String> clientAccessUrlProvider;
    private final WebPubSubProtocolType webPubSubProtocol;
    private final boolean autoReconnect;
    private final boolean autoRestoreGroup;

    // client
    private final String applicationId;
    private final ClientEndpointConfiguration clientEndpointConfiguration;

    // websocket client
    private final WebSocketClient webSocketClient;
    private WebSocketSession webSocketSession;

    // Reactor messages
    private Sinks.Many<GroupMessageEvent> groupMessageEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<ServerMessageEvent> serverMessageEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<AckMessage> ackMessageSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<ConnectedEvent> connectedEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<DisconnectedEvent> disconnectedEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<StoppedEvent> stoppedEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<RejoinGroupFailedEvent> rejoinGroupFailedEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    // incremental ackId
    private final AtomicLong ackId = new AtomicLong(0);

    // connection (logic, one to one map to the connectionId)
    private WebPubSubConnection webPubSubConnection;

    // sequence ack task
    private final AtomicReference<Disposable> sequenceAckTask = new AtomicReference<>();

    // client state
    private final ClientState clientState = new ClientState();
    // state on close
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();
    // state on stop by user
    private final AtomicBoolean isStoppedByUser = new AtomicBoolean();
    private final AtomicReference<Sinks.Empty<Void>> isStoppedByUserSink = new AtomicReference<>();

    // groups
    private final ConcurrentMap<String, WebPubSubGroup> groups = new ConcurrentHashMap<>();

    // retry
    private final Retry sendMessageRetrySpec;

    private static final Duration ACK_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration RECOVER_TIMEOUT = Duration.ofSeconds(30);
    private static final Retry RECONNECT_RETRY_SPEC =
        Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
            .filter(thr -> !(thr instanceof StopReconnectException));

    // delay
    private static final Duration CLOSE_AFTER_SESSION_OPEN_DELAY = Duration.ofMillis(100);
    private static final Duration SEQUENCE_ACK_DELAY = Duration.ofSeconds(5);

    WebPubSubAsyncClient(WebSocketClient webSocketClient,
                         Supplier<String> clientAccessUrlSupplier,
                         WebPubSubProtocolType webPubSubProtocol,
                         String applicationId, String userAgent,
                         RetryStrategy retryStrategy,
                         boolean autoReconnect,
                         boolean autoRestoreGroup) {

        updateLogger(applicationId, null);

        this.applicationId = applicationId;

        // options
        Objects.requireNonNull(clientAccessUrlSupplier);
        this.clientAccessUrlProvider = Mono.fromSupplier(clientAccessUrlSupplier)
            .subscribeOn(Schedulers.boundedElastic());
        this.webPubSubProtocol = Objects.requireNonNull(webPubSubProtocol);
        this.autoReconnect = autoReconnect;
        this.autoRestoreGroup = autoRestoreGroup;

        // websocket configuration and client
        this.clientEndpointConfiguration = new ClientEndpointConfiguration(webPubSubProtocol.toString(), userAgent);
        this.webSocketClient = webSocketClient == null ? new WebSocketClientNettyImpl() : webSocketClient;

        this.sendMessageRetrySpec = Retry.from(signals -> {
            AtomicInteger retryCount = new AtomicInteger(0);
            return signals.concatMap(s -> {
                Mono<Retry.RetrySignal> ret = Mono.error(s.failure());
                if (s.failure() instanceof SendMessageFailedException) {
                    if (((SendMessageFailedException) s.failure()).isTransient()) {
                        int retryAttempt = retryCount.incrementAndGet();
                        if (retryAttempt <= retryStrategy.getMaxRetries()) {
                            ret = Mono.delay(retryStrategy.calculateRetryDelay(retryAttempt))
                                .then(Mono.just(s));
                        }
                    }
                }
                return ret;
            });
        });
    }

    /**
     * Gets the connection ID.
     *
     * @return the connection ID.
     */
    public String getConnectionId() {
        return webPubSubConnection == null ? null : webPubSubConnection.getConnectionId();
    }

    /**
     * Starts the client for connecting to the server.
     *
     * @return the task.
     */
    public Mono<Void> start() {
        return this.start(null);
    }

    Mono<Void> start(Runnable postStartTask) {
        if (clientState.get() == WebPubSubClientState.CLOSED) {
            return Mono.error(logger.logExceptionAsError(
                new IllegalStateException("Failed to start. Client is CLOSED.")));
        }
        return Mono.defer(() -> {
            logger.atInfo()
                .addKeyValue("currentClientState", clientState.get())
                .log("Start client called.");

            isStoppedByUser.set(false);
            isStoppedByUserSink.set(null);

            boolean success = clientState.changeStateOn(WebPubSubClientState.STOPPED, WebPubSubClientState.CONNECTING);
            if (!success) {
                return Mono.error(logger.logExceptionAsError(
                    new IllegalStateException("Failed to start. Client is not STOPPED.")));
            } else {
                if (postStartTask != null) {
                    postStartTask.run();
                }

                return Mono.empty();
            }
        }).then(clientAccessUrlProvider.flatMap(url -> Mono.<Void>fromRunnable(() -> {
            // open connection from client
            this.webSocketSession = webSocketClient.connectToServer(
                clientEndpointConfiguration, url, loggerReference,
                this::handleMessage, this::handleSessionOpen, this::handleSessionClose);
        }).subscribeOn(Schedulers.boundedElastic()))).doOnError(error -> {
            // stop if error, do not send StoppedEvent when it fails at start(), which would have exception thrown
            handleClientStop(false);
        });
    }

    /**
     * Stops the client for disconnecting from the server.
     *
     * @return the task.
     */
    public Mono<Void> stop() {
        if (clientState.get() == WebPubSubClientState.CLOSED) {
            return Mono.error(logger.logExceptionAsError(
                new IllegalStateException("Failed to stop. Client is CLOSED.")));
        }
        return Mono.defer(() -> {
            logger.atInfo()
                .addKeyValue("currentClientState", clientState.get())
                .log("Stop client called.");

            if (clientState.get() == WebPubSubClientState.STOPPED) {
                // already STOPPED
                return Mono.empty();
            } else if (clientState.get() == WebPubSubClientState.STOPPING) {
                // already STOPPING
                // isStoppedByUserMono will be signaled in handleClientStop
                return getStoppedByUserMono();
            }

            // reset
            isStoppedByUser.compareAndSet(false, true);
            groups.clear();

            WebSocketSession localSession = webSocketSession;
            if (localSession != null && localSession.isOpen()) {
                // should be CONNECTED
                clientState.changeState(WebPubSubClientState.STOPPING);
                return Mono.fromCallable(() -> {
                    localSession.close();
                    return (Void) null;
                }).subscribeOn(Schedulers.boundedElastic());
            } else {
                if (clientState.changeStateOn(WebPubSubClientState.DISCONNECTED, WebPubSubClientState.STOPPED)) {
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
        });
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
                this.clientState.changeState(WebPubSubClientState.CLOSED);

                isClosedMono.emitEmpty(emitFailureHandler("Unable to emit Close"));
            })).block();
        }
    }

    /**
     * Joins a group.
     *
     * @param group the group name.
     * @return the result.
     */
    public Mono<WebPubSubResult> joinGroup(String group) {
        return joinGroup(group, nextAckId());
    }

    /**
     * Joins a group.
     *
     * @param group the group name.
     * @param ackId the ackId. Client will provide auto increment ID, if set to {@code null}.
     * @return the result.
     */
    public Mono<WebPubSubResult> joinGroup(String group, Long ackId) {
        Objects.requireNonNull(group);
        if (ackId == null) {
            ackId = nextAckId();
        }
        return sendMessage(new JoinGroupMessage().setGroup(group).setAckId(ackId))
            .then(waitForAckMessage(ackId)).retryWhen(sendMessageRetrySpec)
            .map(result -> {
                groups.compute(group, (k, v) -> {
                    if (v == null) {
                        return new WebPubSubGroup(group).setJoined(true);
                    } else {
                        return v.setJoined(true);
                    }
                });
                return result;
            });
    }

    /**
     * Leaves a group.
     *
     * @param group the group name.
     * @return the result.
     */
    public Mono<WebPubSubResult> leaveGroup(String group) {
        return leaveGroup(group, nextAckId());
    }

    /**
     * Leaves a group.
     *
     * @param group the group name.
     * @param ackId the ackId. Client will provide auto increment ID, if set to {@code null}.
     * @return the result.
     */
    public Mono<WebPubSubResult> leaveGroup(String group, Long ackId) {
        Objects.requireNonNull(group);
        if (ackId == null) {
            ackId = nextAckId();
        }
        return sendMessage(new LeaveGroupMessage().setGroup(group).setAckId(ackId))
            .then(waitForAckMessage(ackId)).retryWhen(sendMessageRetrySpec)
            .map(result -> {
                groups.compute(group, (k, v) -> {
                    if (v == null) {
                        return new WebPubSubGroup(group).setJoined(false);
                    } else {
                        return v.setJoined(false);
                    }
                });
                return result;
            });
    }

    /**
     * Sends message to group.
     *
     * @param group the group name.
     * @param content the data as WebPubSubDataType.TEXT.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendToGroup(String group, String content) {
        return sendToGroup(group, BinaryData.fromString(content), WebPubSubDataFormat.TEXT);
    }

    /**
     * Sends message to group.
     *
     * @param group the group name.
     * @param content the data as WebPubSubDataType.TEXT.
     * @param options the options.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendToGroup(String group, String content, SendToGroupOptions options) {
        return sendToGroup(group, BinaryData.fromString(content), WebPubSubDataFormat.TEXT, options);
    }

    /**
     * Sends message to group.
     *
     * @param group the group name.
     * @param content the data.
     * @param dataFormat the data format.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendToGroup(String group, BinaryData content, WebPubSubDataFormat dataFormat) {
        return sendToGroup(group, content, dataFormat, new SendToGroupOptions().setAckId(nextAckId()));
    }

    /**
     * Sends message to group.
     *
     * @param group the group name.
     * @param content the data.
     * @param dataFormat the data format.
     * @param options the options.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendToGroup(String group, BinaryData content, WebPubSubDataFormat dataFormat,
                                             SendToGroupOptions options) {
        Objects.requireNonNull(group);
        Objects.requireNonNull(content);
        Objects.requireNonNull(dataFormat);
        Objects.requireNonNull(options);

        Long ackId = options.isFireAndForget()
            ? null
            : (options.getAckId() != null ? options.getAckId() : nextAckId());

        SendToGroupMessage message = new SendToGroupMessage()
            .setGroup(group)
            .setData(content)
            .setDataType(dataFormat.toString())
            .setAckId(ackId)
            .setNoEcho(options.isEchoDisabled());

        Mono<Void> sendMessageMono = sendMessage(message);
        Mono<WebPubSubResult> responseMono = sendMessageMono.then(waitForAckMessage(ackId));
        return responseMono.retryWhen(sendMessageRetrySpec);
    }

    /**
     * Sends event.
     *
     * @param eventName the event name.
     * @param content the data.
     * @param dataFormat the data format.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendEvent(String eventName, BinaryData content, WebPubSubDataFormat dataFormat) {
        return sendEvent(eventName, content, dataFormat, new SendEventOptions().setAckId(nextAckId()));
    }

    /**
     * Sends event.
     *
     * @param eventName the event name.
     * @param content the data.
     * @param dataFormat the data format.
     * @param options the options.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendEvent(String eventName, BinaryData content, WebPubSubDataFormat dataFormat,
                                           SendEventOptions options) {
        Objects.requireNonNull(eventName);
        Objects.requireNonNull(content);
        Objects.requireNonNull(dataFormat);
        Objects.requireNonNull(options);

        Long ackId = options.isFireAndForget()
            ? null
            : (options.getAckId() != null ? options.getAckId() : nextAckId());

        SendEventMessage message = new SendEventMessage()
            .setEvent(eventName)
            .setData(content)
            .setDataType(dataFormat.toString())
            .setAckId(ackId);

        Mono<Void> sendMessageMono = sendMessage(message);
        Mono<WebPubSubResult> responseMono = sendMessageMono.then(waitForAckMessage(ackId));
        return responseMono.retryWhen(sendMessageRetrySpec);
    }

    /**
     * Receives group message events.
     *
     * @return the Publisher of group message events.
     */
    public Flux<GroupMessageEvent> receiveGroupMessageEvents() {
        return groupMessageEventSink.asFlux();
    }

    /**
     * Receives server message events.
     *
     * @return the Publisher of server message events.
     */
    public Flux<ServerMessageEvent> receiveServerMessageEvents() {
        return serverMessageEventSink.asFlux();
    }

    /**
     * Receives connected events.
     *
     * @return the Publisher of connected events.
     */
    public Flux<ConnectedEvent> receiveConnectedEvents() {
        return connectedEventSink.asFlux();
    }

    /**
     * Receives disconnected events.
     *
     * @return the Publisher of disconnected events.
     */
    public Flux<DisconnectedEvent> receiveDisconnectedEvents() {
        return disconnectedEventSink.asFlux();
    }

    /**
     * Receives stopped events.
     *
     * @return the Publisher of stopped events.
     */
    public Flux<StoppedEvent> receiveStoppedEvents() {
        return stoppedEventSink.asFlux();
    }

    /**
     * Receives re-join group failed events.
     *
     * @return the Publisher of re-join failed events.
     */
    public Flux<RejoinGroupFailedEvent> receiveRejoinGroupFailedEvents() {
        return rejoinGroupFailedEventSink.asFlux();
    }

    private long nextAckId() {
        return ackId.getAndUpdate(value -> {
            // keep positive
            if (++value < 0) {
                value = 0;
            }
            return value;
        });
    }

    private Flux<AckMessage> receiveAckMessages() {
        return ackMessageSink.asFlux();
    }

    private Mono<Void> sendMessage(WebPubSubMessage message) {
        return checkStateBeforeSend().then(Mono.create(sink -> {
//            if (logger.canLogAtLevel(LogLevel.VERBOSE)) {
//                try {
//                    String json = JacksonAdapter.createDefaultSerializerAdapter()
//                        .serialize(message, SerializerEncoding.JSON);
//                    logger.atVerbose().addKeyValue("message", json).log("Send message");
//                } catch (IOException e) {
//                    sink.error(new UncheckedIOException("Failed to serialize message for VERBOSE logging", e));
//                }
//            }

            webSocketSession.sendObjectAsync(message, sendResult -> {
                if (sendResult.isOK()) {
                    sink.success();
                } else {
                    sink.error(logSendMessageFailedException(
                        "Failed to send message.", sendResult.getException(), true, message));
                }
            });
        }));
    }

    private Mono<Void> checkStateBeforeSend() {
        return Mono.defer(() -> {
            WebPubSubClientState state = clientState.get();
            if (state == WebPubSubClientState.CLOSED) {
                return Mono.error(logger.logExceptionAsError(
                    new IllegalStateException("Failed to send message. WebPubSubClient is CLOSED.")));
            }
            if (state != WebPubSubClientState.CONNECTED) {
                return Mono.error(logSendMessageFailedException(
                    "Failed to send message. Client is " + state.name() + ".",
                    null,
                    state == WebPubSubClientState.RECOVERING
                        || state == WebPubSubClientState.CONNECTING
                        || state == WebPubSubClientState.RECONNECTING
                        || state == WebPubSubClientState.DISCONNECTED,
                    (Long) null));
            }
            if (webSocketSession == null || !webSocketSession.isOpen()) {
                // something unexpected
                return Mono.error(logSendMessageFailedException(
                    "Failed to send message. Websocket session is not opened.", null, false, (Long) null));
            } else {
                return Mono.empty();
            }
        });
    }

    private Mono<Void> getStoppedByUserMono() {
        Sinks.Empty<Void> sink = Sinks.empty();
        boolean isStoppedByUserMonoSet = isStoppedByUserSink.compareAndSet(null, sink);
        if (!isStoppedByUserMonoSet) {
            sink = isStoppedByUserSink.get();
        }
        return sink == null ? Mono.empty() : sink.asMono();
    }

    private void tryCompleteOnStoppedByUserSink() {
        // clear isStoppedByUserMono
        Sinks.Empty<Void> mono = isStoppedByUserSink.getAndSet(null);
        if (mono != null) {
            mono.emitEmpty(emitFailureHandler("Unable to emit Stopped"));
        }
    }

    private <EventT> void tryEmitNext(Sinks.Many<EventT> sink, EventT event) {
        logger.atVerbose()
            .addKeyValue("type", event.getClass().getSimpleName())
            .log("Send event");
        sink.emitNext(event,
            emitFailureHandler("Unable to emit " + event.getClass().getSimpleName()));
    }

    private Mono<WebPubSubResult> waitForAckMessage(Long ackId) {
        if (ackId == null) {
            // fireAndForget
            return Mono.just(new WebPubSubResult(null, false));
        }
        return receiveAckMessages()
            .filter(m -> ackId == m.getAckId())
            // single AckMessage
            .next()
            // error from upstream
            .onErrorMap(throwable -> logSendMessageFailedException(
                "Acknowledge from the service not received.", throwable, true, ackId))
            // error from AckMessage
            .flatMap(m -> {
                if (m.isSuccess()) {
                    return Mono.just(new WebPubSubResult(m.getAckId(), false));
                } else if (m.getError() != null && "Duplicate".equals(m.getError().getName())) {
                    return Mono.just(new WebPubSubResult(m.getAckId(), true));
                } else {
                    return Mono.error(logSendMessageFailedException(
                        "Received non-success acknowledge from the service.", null, false, ackId, m.getError()));
                }
            })
            // timeout or stream closed
            .timeout(ACK_TIMEOUT, Mono.empty())
            .switchIfEmpty(Mono.defer(() -> Mono.error(logSendMessageFailedException(
                "Acknowledge from the service not received.", null, true, ackId))));
    }

    private void handleSessionOpen(WebSocketSession session) {
        logger.atVerbose().log("Session opened");

        clientState.changeState(WebPubSubClientState.CONNECTED);

        if (isStoppedByUser.compareAndSet(true, false)) {
            // user intended to stop, but issued when session is not CONNECTED or STOPPED,
            // e.g. CONNECTING, RECOVERING, RECONNECTING

            // delay a bit, as handleSessionOpen is in websocket callback
            Mono.delay(CLOSE_AFTER_SESSION_OPEN_DELAY).then(Mono.fromCallable(() -> {
                clientState.changeState(WebPubSubClientState.STOPPING);

                if (session != null && session.isOpen()) {
                    session.close();
                } else {
                    logger.atError()
                        .log("Failed to close session after session open");
                    handleClientStop();
                }
                return (Void) null;
            }).subscribeOn(Schedulers.boundedElastic())).subscribe(null, thr -> {
                logger.atError()
                    .log("Failed to close session after session open: " + thr.getMessage());
                // force a stopped state
                handleClientStop();
            });
        } else {
            // sequence ack task, for reliable protocol
            if (isReliableProtocol(webPubSubProtocol)) {
                Flux<Void> sequenceAckFlux = Flux.interval(SEQUENCE_ACK_DELAY).concatMap(ignored -> {
                    if (clientState.get() == WebPubSubClientState.CONNECTED && session != null && session.isOpen()) {
                        WebPubSubConnection connection = this.webPubSubConnection;
                        if (connection != null) {
                            Long id = connection.getSequenceAckId().getUpdated();
                            if (id != null) {
                                return sendMessage(new SequenceAckMessage().setSequenceId(id))
                                    .onErrorResume(error -> {
                                        // ignore error, wait for next chance
                                        connection.getSequenceAckId().setUpdated();
                                        return Mono.empty();
                                    });
                            } else {
                                return Mono.empty();
                            }
                        } else {
                            return Mono.empty();
                        }
                    } else {
                        return Mono.empty();
                    }
                });

                Disposable previousTask = sequenceAckTask.getAndSet(sequenceAckFlux.subscribe());
                if (previousTask != null) {
                    previousTask.dispose();
                }
            }

            // restore group
            if (autoRestoreGroup) {
                List<Mono<WebPubSubResult>> restoreGroupMonoList = groups.values().stream()
                    .filter(WebPubSubGroup::isJoined)
                    .map(group -> joinGroup(group.getName()).onErrorResume(error -> {
                        if (error instanceof SendMessageFailedException) {
                            tryEmitNext(rejoinGroupFailedEventSink,
                                new RejoinGroupFailedEvent(group.getName(), (SendMessageFailedException) error));
                        }
                        return Mono.empty();
                    }))
                    .collect(Collectors.toList());

                // delay a bit, as handleSessionOpen is in websocket callback
                Mono.delay(CLOSE_AFTER_SESSION_OPEN_DELAY)
                    .thenMany(Flux.mergeSequentialDelayError(restoreGroupMonoList,
                        Schedulers.DEFAULT_POOL_SIZE, Schedulers.DEFAULT_POOL_SIZE))
                    .subscribe(null, thr -> {
                        logger.atWarning()
                            .log("Failed to auto restore group: " + thr.getMessage());
                    });
            }
        }
    }

    private void handleSessionClose(CloseReason closeReason) {
        logger.atVerbose().addKeyValue("code", closeReason.getCloseCode()).log("Session closed");

        final int violatedPolicyStatusCode = 1008;

        if (clientState.get() == WebPubSubClientState.STOPPED) {
            return;
        }

        final String connectionId = this.getConnectionId();

        if (isStoppedByUser.compareAndSet(true, false)
            || clientState.get() == WebPubSubClientState.STOPPING) {
            // connection close, send DisconnectedEvent
            handleConnectionClose();

            // stopped by user
            handleClientStop();
        } else if (closeReason.getCloseCode() == violatedPolicyStatusCode) {
            clientState.changeState(WebPubSubClientState.DISCONNECTED);
            // connection close, send DisconnectedEvent
            handleConnectionClose();

            // reconnect
            handleNoRecovery().subscribe(null, thr -> {
                logger.atWarning()
                    .log("Failed to auto reconnect session: " + thr.getMessage());
            });
        } else {
            final WebPubSubConnection connection = this.webPubSubConnection;
            final String reconnectionToken = connection == null ? null : connection.getReconnectionToken();
            if (!isReliableProtocol(webPubSubProtocol) || reconnectionToken == null || connectionId == null) {
                clientState.changeState(WebPubSubClientState.DISCONNECTED);
                // connection close, send DisconnectedEvent
                handleConnectionClose();

                // reconnect
                handleNoRecovery().subscribe(null, thr -> {
                    logger.atWarning()
                        .log("Failed to auto reconnect session: " + thr.getMessage());
                });
            } else {
                // connection not close, attempt recover
                handleRecovery(connectionId, reconnectionToken).timeout(RECOVER_TIMEOUT, Mono.defer(() -> {
                    // fallback to reconnect

                    // client should be RECOVERING, after timeout
                    clientState.changeState(WebPubSubClientState.DISCONNECTED);
                    // connection close, send DisconnectedEvent
                    handleConnectionClose();

                    return handleNoRecovery();
                })).subscribe(null, thr -> {
                    logger.atWarning()
                        .log("Failed to recover or reconnect session: " + thr.getMessage());
                });
            }
        }
    }

    private void handleMessage(Object webPubSubMessage) {
//        if (logger.canLogAtLevel(LogLevel.VERBOSE)) {
//            try {
//                String json = JacksonAdapter.createDefaultSerializerAdapter()
//                    .serialize(webPubSubMessage, SerializerEncoding.JSON);
//                logger.atVerbose().addKeyValue("message", json).log("Received message");
//            } catch (IOException e) {
//                throw logger.logExceptionAsError(
//                    new UncheckedIOException("Failed to serialize received message for VERBOSE logging", e));
//            }
//        }

        if (webPubSubMessage instanceof GroupDataMessage) {
            final GroupDataMessage groupDataMessage = (GroupDataMessage) webPubSubMessage;

            boolean emitMessage = true;
            if (groupDataMessage.getSequenceId() != null) {
                emitMessage = updateSequenceAckId(groupDataMessage.getSequenceId());
            }
            if (emitMessage) {
                tryEmitNext(groupMessageEventSink, new GroupMessageEvent(
                    groupDataMessage.getGroup(),
                    groupDataMessage.getData(),
                    groupDataMessage.getDataType(),
                    groupDataMessage.getFromUserId(),
                    groupDataMessage.getSequenceId()));
            }
        } else if (webPubSubMessage instanceof ServerDataMessage) {
            final ServerDataMessage serverDataMessage = (ServerDataMessage) webPubSubMessage;

            boolean emitMessage = true;
            if (serverDataMessage.getSequenceId() != null) {
                emitMessage = updateSequenceAckId(serverDataMessage.getSequenceId());
            }
            if (emitMessage) {
                tryEmitNext(serverMessageEventSink, new ServerMessageEvent(
                    serverDataMessage.getData(),
                    serverDataMessage.getDataType(),
                    serverDataMessage.getSequenceId()));
            }
        } else if (webPubSubMessage instanceof AckMessage) {
            tryEmitNext(ackMessageSink, (AckMessage) webPubSubMessage);
        } else if (webPubSubMessage instanceof ConnectedMessage) {
            final ConnectedMessage connectedMessage = (ConnectedMessage) webPubSubMessage;
            final String connectionId = connectedMessage.getConnectionId();

            updateLogger(applicationId, connectionId);

            // Create new WebPubSubConnection if absent.
            // ConnectedMessage could be sent by server on recover, when WebPubSubConnection exists in client.
            // In this case, reconnectionToken would be updated, but ConnectedEvent won't be sent.
            if (this.webPubSubConnection == null) {
                this.webPubSubConnection = new WebPubSubConnection();
            }
            this.webPubSubConnection.updateForConnected(
                connectedMessage.getConnectionId(), connectedMessage.getReconnectionToken(),
                () -> tryEmitNext(connectedEventSink, new ConnectedEvent(
                    connectionId,
                    connectedMessage.getUserId())));
        } else if (webPubSubMessage instanceof DisconnectedMessage) {
            final DisconnectedMessage disconnectedMessage = (DisconnectedMessage) webPubSubMessage;
            // send DisconnectedEvent, but connection close will be handled in handleSessionClose
            handleConnectionClose(new DisconnectedEvent(
                this.getConnectionId(),
                disconnectedMessage.getReason()));
        }
    }

    private boolean updateSequenceAckId(long id) {
        WebPubSubConnection connection = this.webPubSubConnection;
        if (connection != null) {
            return connection.getSequenceAckId().update(id);
        } else {
            // this should not happen
            return false;
        }
    }

    private Mono<Void> handleNoRecovery() {
        return Mono.defer(() -> {
            if (isStoppedByUser.compareAndSet(true, false)) {
                // stopped by user
                handleClientStop();
                return Mono.empty();
            } else if (autoReconnect) {
                // try reconnect

                boolean success = clientState.changeStateOn(WebPubSubClientState.DISCONNECTED,
                    WebPubSubClientState.RECONNECTING);
                if (!success) {
                    return Mono.error(logger.logExceptionAsError(
                        new StopReconnectException("Failed to start. Client is not DISCONNECTED.")));
                }

                return Mono.defer(() -> {
                    if (isStoppedByUser.compareAndSet(true, false)) {
                        return Mono.error(logger.logExceptionAsWarning(
                            new StopReconnectException("Client is stopped by user.")));
                    } else {
                        return Mono.empty();
                    }
                }).then(clientAccessUrlProvider.flatMap(url -> Mono.<Void>fromRunnable(() -> {
                    this.webSocketSession = webSocketClient.connectToServer(
                        clientEndpointConfiguration, url, loggerReference,
                        this::handleMessage, this::handleSessionOpen, this::handleSessionClose);
                }).subscribeOn(Schedulers.boundedElastic()))).retryWhen(RECONNECT_RETRY_SPEC).doOnError(error -> {
                    // stopped by user
                    handleClientStop();
                });
            } else {
                handleClientStop();
                return Mono.empty();
            }
        });
    }

    private Mono<Void> handleRecovery(String connectionId, String reconnectionToken) {
        return Mono.defer(() -> {
            if (isStoppedByUser.compareAndSet(true, false)) {
                // stopped by user
                handleClientStop();
                return Mono.empty();
            } else {
                // try recovery

                boolean success = clientState.changeStateOn(WebPubSubClientState.CONNECTED,
                    WebPubSubClientState.RECOVERING);
                if (!success) {
                    return Mono.error(logger.logExceptionAsError(
                        new StopReconnectException("Failed to recover. Client is not CONNECTED.")));
                }

                return Mono.defer(() -> {
                    if (isStoppedByUser.compareAndSet(true, false)) {
                        return Mono.error(logger.logExceptionAsWarning(
                            new StopReconnectException("Client is stopped by user.")));
                    } else {
                        return Mono.empty();
                    }
                }).then(clientAccessUrlProvider.flatMap(url -> Mono.<Void>fromRunnable(() -> {
                    String recoveryUrl = UrlBuilder.parse(url)
                        .addQueryParameter("awps_connection_id", connectionId)
                        .addQueryParameter("awps_reconnection_token", reconnectionToken)
                        .toString();

                    this.webSocketSession = webSocketClient.connectToServer(
                        clientEndpointConfiguration, recoveryUrl, loggerReference,
                        this::handleMessage, this::handleSessionOpen, this::handleSessionClose);
                }).subscribeOn(Schedulers.boundedElastic()))).retryWhen(RECONNECT_RETRY_SPEC).doOnError(error -> {
                    // stopped by user
                    handleClientStop();
                });
            }
        });
    }

    private void handleClientStop() {
        handleClientStop(true);
    }

    private void handleClientStop(boolean sendStoppedEvent) {
        clientState.changeState(WebPubSubClientState.STOPPED);

        // session
        this.webSocketSession = null;
        // logic connection
        this.webPubSubConnection = null;

        tryCompleteOnStoppedByUserSink();

        // stop sequence ack task
        Disposable task = sequenceAckTask.getAndSet(null);
        if (task != null) {
            task.dispose();
        }

        // send StoppedEvent
        if (sendStoppedEvent) {
            tryEmitNext(stoppedEventSink, new StoppedEvent());
        }

        groupMessageEventSink.emitComplete(
            emitFailureHandler("Unable to emit Complete to groupMessageEventSink"));
        groupMessageEventSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

        serverMessageEventSink.emitComplete(
            emitFailureHandler("Unable to emit Complete to groupMessageEventSink"));
        serverMessageEventSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

        connectedEventSink.emitComplete(
            emitFailureHandler("Unable to emit Complete to connectedEventSink"));
        connectedEventSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

        disconnectedEventSink.emitComplete(
            emitFailureHandler("Unable to emit Complete to disconnectedEventSink"));
        disconnectedEventSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

        stoppedEventSink.emitComplete(
            emitFailureHandler("Unable to emit Complete to disconnectedEventSink"));
        stoppedEventSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

        rejoinGroupFailedEventSink.emitComplete(
            emitFailureHandler("Unable to emit Complete to rejoinGroupFailedEventSink"));
        rejoinGroupFailedEventSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

        ackMessageSink.emitComplete(emitFailureHandler("Unable to emit Complete to ackMessageSink"));
        ackMessageSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

        updateLogger(applicationId, null);
    }

    private void handleConnectionClose() {
        handleConnectionClose(null);
    }

    private void handleConnectionClose(DisconnectedEvent disconnectedEvent) {
        final DisconnectedEvent event = disconnectedEvent == null
            ? new DisconnectedEvent(this.getConnectionId(), null)
            : disconnectedEvent;

        WebPubSubConnection connection = this.webPubSubConnection;
        if (connection != null) {
            connection.updateForDisconnected(() -> tryEmitNext(disconnectedEventSink, event));
        }

        if (disconnectedEvent == null) {
            // Called from handleSessionClose, clear WebPubSubConnection.
            // It means client now forget this WebPubSubConnection, include connectionId and sequenceId.
            this.webPubSubConnection = null;
        }
    }

    private void updateLogger(String applicationId, String connectionId) {
        logger = new ClientLogger(WebPubSubAsyncClient.class,
            LoggingUtils.createContextWithConnectionId(applicationId, connectionId));
        loggerReference.set(logger);
    }

    private static final class StopReconnectException extends RuntimeException {
        private StopReconnectException(String message) {
            super(message);
        }
    }

    private final class ClientState {

        private final AtomicReference<WebPubSubClientState> clientState =
            new AtomicReference<>(WebPubSubClientState.STOPPED);

        WebPubSubClientState get() {
            return clientState.get();
        }

        WebPubSubClientState changeState(WebPubSubClientState state) {
            WebPubSubClientState previousState = clientState.getAndSet(state);
            logger.atInfo()
                .addKeyValue("currentClientState", state)
                .addKeyValue("previousClientState", previousState)
                .log("Client state changed.");
            return previousState;
        }

        boolean changeStateOn(WebPubSubClientState previousState, WebPubSubClientState state) {
            boolean success = clientState.compareAndSet(previousState, state);
            if (success) {
                logger.atInfo()
                    .addKeyValue("currentClientState", state)
                    .addKeyValue("previousClientState", previousState)
                    .log("Client state changed.");
            }
            return success;
        }
    }

    WebPubSubClientState getClientState() {
        return clientState.get();
    }

    WebSocketSession getWebsocketSession() {
        return webSocketSession;
    }

    private Sinks.EmitFailureHandler emitFailureHandler(String message) {
        return (signalType, emitResult) -> {
            LoggingUtils.addSignalTypeAndResult(this.logger.atWarning(), signalType, emitResult)
                .log(message);
            return emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED);
        };
    }

    private RuntimeException logSendMessageFailedException(
        String errorMessage, Throwable cause, boolean isTransient, WebPubSubMessage message) {

        return logSendMessageFailedException(errorMessage, cause, isTransient,
            (message instanceof WebPubSubMessageAck) ? ((WebPubSubMessageAck) message).getAckId() : null);
    }

    private RuntimeException logSendMessageFailedException(
        String errorMessage, Throwable cause, boolean isTransient, Long ackId) {

        return logSendMessageFailedException(errorMessage, cause, isTransient, ackId, null);
    }

    private RuntimeException logSendMessageFailedException(
        String errorMessage, Throwable cause, boolean isTransient, Long ackId, AckResponseError error) {

        return logger.logExceptionAsWarning(
            new SendMessageFailedException(errorMessage, cause, isTransient, ackId, error));
    }

    private static boolean isReliableProtocol(WebPubSubProtocolType webPubSubProtocol) {
        return webPubSubProtocol == WebPubSubProtocolType.JSON_RELIABLE_PROTOCOL;
    }
}
