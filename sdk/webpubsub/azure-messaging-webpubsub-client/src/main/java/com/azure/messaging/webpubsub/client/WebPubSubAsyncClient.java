// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.webpubsub.client.implementation.ws.Client;
import com.azure.messaging.webpubsub.client.implementation.ws.ClientEndpointConfiguration;
import com.azure.messaging.webpubsub.client.implementation.ws.ClientNettyImpl;
import com.azure.messaging.webpubsub.client.implementation.ws.CloseReason;
import com.azure.messaging.webpubsub.client.implementation.ws.Session;
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
import com.azure.messaging.webpubsub.client.models.AckMessageError;
import com.azure.messaging.webpubsub.client.models.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.models.JoinGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.models.LeaveGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.models.SendToGroupMessage;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import com.azure.messaging.webpubsub.client.models.DisconnectedEvent;
import com.azure.messaging.webpubsub.client.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.models.GroupMessageEvent;
import com.azure.messaging.webpubsub.client.models.SendEventOptions;
import com.azure.messaging.webpubsub.client.models.SendToGroupOptions;
import com.azure.messaging.webpubsub.client.models.ServerDataMessage;
import com.azure.messaging.webpubsub.client.models.ServerMessageEvent;
import com.azure.messaging.webpubsub.client.models.StoppedEvent;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.implementation.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import com.azure.messaging.webpubsub.client.models.WebPubSubProtocol;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;
import reactor.util.retry.Retry;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * The WebPubSubAsync client.
 */
@ServiceClient(builder = WebPubSubClientBuilder.class)
class WebPubSubAsyncClient implements Closeable {

    // logging
    private ClientLogger logger;
    private final AtomicReference<ClientLogger> loggerReference = new AtomicReference<>();

    // options
    private final Mono<String> clientAccessUrlProvider;
    private final WebPubSubProtocol webPubSubProtocol;
    private final boolean autoReconnect;
    private final boolean autoRestoreGroup;

    // client
    private final String applicationId;
    private final ClientEndpointConfiguration clientEndpointConfiguration;

    // websocket client
    private final Client client;
    private Session session;

    private String connectionId;
    private String reconnectionToken;

    private static final AtomicLong ACK_ID = new AtomicLong(0);

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

    // sequence ack
    private final SequenceAckId sequenceAckId = new SequenceAckId();
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

    WebPubSubAsyncClient(Client client,
                         Mono<String> clientAccessUrlProvider,
                         WebPubSubProtocol webPubSubProtocol,
                         String applicationId, String userAgent,
                         RetryStrategy retryStrategy,
                         boolean autoReconnect,
                         boolean autoRestoreGroup) {

        updateLogger(applicationId, null);

        this.applicationId = applicationId;

        this.clientEndpointConfiguration = new ClientEndpointConfiguration(webPubSubProtocol.getName(), userAgent);

        this.clientAccessUrlProvider = Objects.requireNonNull(clientAccessUrlProvider);
        this.webPubSubProtocol = Objects.requireNonNull(webPubSubProtocol);
        this.autoReconnect = autoReconnect;
        this.autoRestoreGroup = autoRestoreGroup;

        this.client = client == null ? new ClientNettyImpl() : client;

        Objects.requireNonNull(retryStrategy);
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
        return connectionId;
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

                // reset
                sequenceAckId.clear();
                return Mono.empty();
            }
        }).then(clientAccessUrlProvider.flatMap(url -> Mono.<Void>fromRunnable(() -> {
            this.session = client.connectToServer(
                clientEndpointConfiguration, url, loggerReference,
                this::handleMessage, this::handleSessionOpen, this::handleSessionClose);
        }).subscribeOn(Schedulers.boundedElastic()))).doOnError(error -> {
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

            Session localSession = session;
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
     * @param ackId the ackId.
     * @return the result.
     */
    public Mono<WebPubSubResult> joinGroup(String group, long ackId) {
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
     * @param ackId the ackId.
     * @return the result.
     */
    public Mono<WebPubSubResult> leaveGroup(String group, long ackId) {
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
        return sendToGroup(group, BinaryData.fromString(content), WebPubSubDataType.TEXT);
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
        return sendToGroup(group, BinaryData.fromString(content), WebPubSubDataType.TEXT, options);
    }

    /**
     * Sends message to group.
     *
     * @param group the group name.
     * @param content the data.
     * @param dataType the data type.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return sendToGroup(group, content, dataType, new SendToGroupOptions().setAckId(nextAckId()));
    }

    /**
     * Sends message to group.
     *
     * @param group the group name.
     * @param content the data.
     * @param dataType the data type.
     * @param options the options.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendToGroup(String group, BinaryData content, WebPubSubDataType dataType,
                                             SendToGroupOptions options) {
        Objects.requireNonNull(group);
        Objects.requireNonNull(content);
        Objects.requireNonNull(dataType);
        Objects.requireNonNull(options);

        long ackId = options.getAckId() != null ? options.getAckId() : nextAckId();

        BinaryData data = content;
        if (dataType == WebPubSubDataType.BINARY || dataType == WebPubSubDataType.PROTOBUF) {
            data = BinaryData.fromBytes(Base64.getEncoder().encode(content.toBytes()));
        }

        SendToGroupMessage message = new SendToGroupMessage()
            .setGroup(group)
            .setData(data)
            .setDataType(dataType.name().toLowerCase(Locale.ROOT))
            .setAckId(ackId)
            .setNoEcho(options.isNoEcho());

        Mono<Void> sendMessageMono = sendMessage(message);
        Mono<WebPubSubResult> responseMono = options.isFireAndForget()
            ? sendMessageMono.then(Mono.just(new WebPubSubResult(null, false)))
            : sendMessageMono.then(waitForAckMessage(ackId));
        return responseMono.retryWhen(sendMessageRetrySpec);
    }

    /**
     * Sends event.
     *
     * @param eventName the event name.
     * @param content the data.
     * @param dataType the data type.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendEvent(String eventName, BinaryData content, WebPubSubDataType dataType) {
        return sendEvent(eventName, content, dataType, new SendEventOptions().setAckId(nextAckId()));
    }

    /**
     * Sends event.
     *
     * @param eventName the event name.
     * @param content the data.
     * @param dataType the data type.
     * @param options the options.
     * @return the result.
     */
    public Mono<WebPubSubResult> sendEvent(String eventName, BinaryData content, WebPubSubDataType dataType,
                                           SendEventOptions options) {
        Objects.requireNonNull(eventName);
        Objects.requireNonNull(content);
        Objects.requireNonNull(dataType);
        Objects.requireNonNull(options);

        long ackId = options.getAckId() != null ? options.getAckId() : nextAckId();

        BinaryData data = content;
        if (dataType == WebPubSubDataType.BINARY || dataType == WebPubSubDataType.PROTOBUF) {
            data = BinaryData.fromBytes(Base64.getEncoder().encode(content.toBytes()));
        }

        SendEventMessage message = new SendEventMessage()
            .setEvent(eventName)
            .setData(data)
            .setDataType(dataType.name().toLowerCase(Locale.ROOT))
            .setAckId(ackId);

        Mono<Void> sendMessageMono = sendMessage(message);
        Mono<WebPubSubResult> responseMono = options.isFireAndForget()
            ? sendMessageMono.then(Mono.just(new WebPubSubResult(null, false)))
            : sendMessageMono.then(waitForAckMessage(ackId));
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
        return ACK_ID.getAndUpdate(value -> {
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
            if (logger.canLogAtLevel(LogLevel.VERBOSE)) {
                try {
                    String json = JacksonAdapter.createDefaultSerializerAdapter()
                        .serialize(message, SerializerEncoding.JSON);
                    logger.atVerbose().addKeyValue("message", json).log("Send message");
                } catch (IOException e) {
                    //
                }
            }

            session.sendObjectAsync(message, sendResult -> {
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
                        || state == WebPubSubClientState.RECONNECTING,
                    (Long) null));
            }
            if (session == null || !session.isOpen()) {
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

    private Mono<WebPubSubResult> waitForAckMessage(long ackId) {
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

    private void handleSessionOpen(Session session) {
        logger.atVerbose().log("Session opened");

        clientState.changeState(WebPubSubClientState.CONNECTED);

        if (isStoppedByUser.compareAndSet(true, false)) {
            // user intended to stop, but issued when session is not OPEN or STOPPED,
            // e.g. CONNECTING, RECOVERING, RECONNECTING

            // delay a bit, as handleSessionOpen is in websocket callback
            Mono.delay(Duration.ofSeconds(1)).then(Mono.fromCallable(() -> {
                clientState.changeState(WebPubSubClientState.STOPPING);

                if (session != null && session.isOpen()) {
                    session.close();
                }
                return (Void) null;
            }).subscribeOn(Schedulers.boundedElastic())).subscribe(null, thr -> {
                logger.atError()
                    .log("Failed to close session: " + thr.getMessage());
                // force a stopped state
                handleClientStop();
            });
        } else {
            // sequenceAck task
            if (webPubSubProtocol.isReliable()) {
                Flux<Void> sequenceAckFlux = Flux.interval(Duration.ofSeconds(5)).concatMap(ignored -> {
                    if (clientState.get() == WebPubSubClientState.CONNECTED && session != null && session.isOpen()) {
                        Long id = sequenceAckId.getUpdated();
                        if (id != null) {
                            return sendMessage(new SequenceAckMessage().setSequenceId(id));
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
                        if (error instanceof Exception) {
                            tryEmitNext(rejoinGroupFailedEventSink,
                                new RejoinGroupFailedEvent(group.getName(), (Exception) error));
                        }
                        return Mono.empty();
                    }))
                    .collect(Collectors.toList());

                Flux.mergeSequentialDelayError(restoreGroupMonoList,
                    Schedulers.DEFAULT_POOL_SIZE, Schedulers.DEFAULT_POOL_SIZE)
                    .subscribeOn(Schedulers.boundedElastic()).subscribe(null, thr -> {
                        logger.atWarning()
                            .log("Failed to auto restore group: " + thr.getMessage());
                    });
            }
        }
    }

    private void handleSessionClose(CloseReason closeReason) {
        logger.atVerbose().addKeyValue("code", closeReason.getCloseCode()).log("Session closed");

        if (clientState.get() == WebPubSubClientState.STOPPED) {
            return;
        }

        if (isStoppedByUser.compareAndSet(true, false)
            || clientState.get() == WebPubSubClientState.STOPPING) {
            // send DisconnectedEvent
            tryEmitNext(disconnectedEventSink, new DisconnectedEvent(connectionId, null));

            // stopped by user
            handleClientStop();
        } else if (closeReason.getCloseCode() == 1008) {
            // do not send DisconnectedEvent
            // server likely send the DisconnectedMessage before close

            // VIOLATED_POLICY
            handleClientStop();
        } else {
            if (!webPubSubProtocol.isReliable() || reconnectionToken == null || connectionId == null) {
                clientState.changeState(WebPubSubClientState.DISCONNECTED);
                // send DisconnectedEvent
                tryEmitNext(disconnectedEventSink, new DisconnectedEvent(connectionId, null));

                handleNoRecovery().subscribe(null, thr -> {
                    logger.atWarning()
                        .log("Failed to auto reconnect session: " + thr.getMessage());
                });
            } else {
                handleRecovery().timeout(RECOVER_TIMEOUT, Mono.defer(() -> {
                    // client should be RECOVERING, after timeout
                    clientState.changeState(WebPubSubClientState.DISCONNECTED);
                    // send DisconnectedEvent
                    tryEmitNext(disconnectedEventSink, new DisconnectedEvent(connectionId, null));

                    // fallback
                    return handleNoRecovery();
                })).subscribe(null, thr -> {
                    logger.atWarning()
                        .log("Failed to recover or reconnect session: " + thr.getMessage());
                });
            }
        }
    }

    private void handleMessage(Object webPubSubMessage) {
        if (logger.canLogAtLevel(LogLevel.VERBOSE)) {
            try {
                String json = JacksonAdapter.createDefaultSerializerAdapter()
                    .serialize(webPubSubMessage, SerializerEncoding.JSON);
                logger.atVerbose().addKeyValue("message", json).log("Received message");
            } catch (IOException e) {
                //
            }
        }

        if (webPubSubMessage instanceof GroupDataMessage) {
            GroupDataMessage groupDataMessage = (GroupDataMessage) webPubSubMessage;
            tryEmitNext(groupMessageEventSink, new GroupMessageEvent(groupDataMessage));

            if (groupDataMessage.getSequenceId() != null) {
                sequenceAckId.update(groupDataMessage.getSequenceId());
            }
        } else if (webPubSubMessage instanceof ServerDataMessage) {
            ServerDataMessage serverDataMessage = (ServerDataMessage) webPubSubMessage;
            tryEmitNext(serverMessageEventSink, new ServerMessageEvent(serverDataMessage));

            if (serverDataMessage.getSequenceId() != null) {
                sequenceAckId.update(serverDataMessage.getSequenceId());
            }
        } else if (webPubSubMessage instanceof AckMessage) {
            ackMessageSink.emitNext((AckMessage) webPubSubMessage,
                emitFailureHandler("Unable to emit GroupMessageEvent"));
        } else if (webPubSubMessage instanceof ConnectedMessage) {
            ConnectedMessage connectedMessage = (ConnectedMessage) webPubSubMessage;
            connectionId = connectedMessage.getConnectionId();
            reconnectionToken = connectedMessage.getReconnectionToken();

            updateLogger(applicationId, connectionId);

            tryEmitNext(connectedEventSink, new ConnectedEvent(
                connectionId,
                connectedMessage.getUserId()));
        } else if (webPubSubMessage instanceof DisconnectedMessage) {
            tryEmitNext(disconnectedEventSink, new DisconnectedEvent(
                connectionId,
                (DisconnectedMessage) webPubSubMessage));
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
                    this.session = client.connectToServer(
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

    private Mono<Void> handleRecovery() {
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

                    this.session = client.connectToServer(
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

        session = null;

        connectionId = null;
        reconnectionToken = null;

        tryCompleteOnStoppedByUserSink();

        // stop sequenceAckTask
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

    private static final class SequenceAckId {

        private final AtomicLong sequenceId = new AtomicLong(0);
        private final AtomicBoolean updated = new AtomicBoolean(false);

        private void clear() {
            sequenceId.set(0);
            updated.set(false);
        }

        private Long getUpdated() {
            if (updated.compareAndSet(true, false)) {
                return sequenceId.get();
            } else {
                return null;
            }
        }

        private void update(long id) {
            long previousId = sequenceId.getAndUpdate(existId -> Math.max(id, existId));

            if (previousId < id) {
                updated.set(true);
            }
        }
    }

    final class ClientState {

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

    Session getWebsocketSession() {
        return session;
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
        String errorMessage, Throwable cause, boolean isTransient, Long ackId, AckMessageError error) {

        return logger.logExceptionAsWarning(
            new SendMessageFailedException(errorMessage, cause, isTransient, ackId, error));
    }
}
