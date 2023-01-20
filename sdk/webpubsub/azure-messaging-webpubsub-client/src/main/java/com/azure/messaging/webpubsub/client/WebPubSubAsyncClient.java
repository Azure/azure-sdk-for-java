// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.client.exception.SendMessageFailedException;
import com.azure.messaging.webpubsub.client.implementation.AckMessage;
import com.azure.messaging.webpubsub.client.implementation.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.LoggingUtils;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubClientState;
import com.azure.messaging.webpubsub.client.implementation.WebPubSubMessageAck;
import com.azure.messaging.webpubsub.client.models.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.MessageEncoder;
import com.azure.messaging.webpubsub.client.implementation.JoinGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.LeaveGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.SendToGroupMessage;
import com.azure.messaging.webpubsub.client.models.ConnectedEvent;
import com.azure.messaging.webpubsub.client.models.DisconnectedEvent;
import com.azure.messaging.webpubsub.client.models.GroupDataMessage;
import com.azure.messaging.webpubsub.client.models.GroupMessageEvent;
import com.azure.messaging.webpubsub.client.models.SendToGroupOptions;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataType;
import com.azure.messaging.webpubsub.client.models.WebPubSubMessage;
import com.azure.messaging.webpubsub.client.models.WebPubSubResult;
import com.azure.messaging.webpubsub.client.protocol.WebPubSubProtocol;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.core.CloseReasons;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@ServiceClient(builder = WebPubSubClientBuilder.class)
public class WebPubSubAsyncClient implements AsyncCloseable {

    // logging
    private ClientLogger logger;

    // options
    private final Mono<String> clientAccessUriProvider;
    private final WebPubSubProtocol webPubSubProtocol;
    private final RetryStrategy retryStrategy;
    private final boolean autoReconnect;
    private final boolean autoRestoreGroup;

    // websocket client
    private final ClientManager clientManager;
    private Endpoint endpoint;
    private Session session;

    private String connectionId;
    private String reconnectionToken;

    private static final AtomicLong ACK_ID = new AtomicLong(0);

    // Reactor messages
    private final Sinks.Many<GroupMessageEvent> groupMessageEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<AckMessage> ackMessageSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private final Sinks.Many<ConnectedEvent> connectedEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private final Sinks.Many<DisconnectedEvent> disconnectedEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    // state on close
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Sinks.Empty<Void> isClosedMono = Sinks.empty();
    private final ClientState clientState = new ClientState();

    WebPubSubAsyncClient(Mono<String> clientAccessUriProvider,
                         WebPubSubProtocol webPubSubProtocol,
                         RetryStrategy retryStrategy,
                         boolean autoReconnect,
                         boolean autoRestoreGroup) {

        this.logger = new ClientLogger(WebPubSubAsyncClient.class);

        this.clientAccessUriProvider = clientAccessUriProvider;
        this.webPubSubProtocol = webPubSubProtocol;
        this.retryStrategy = retryStrategy;
        this.autoReconnect = autoReconnect;
        this.autoRestoreGroup = autoRestoreGroup;

        this.clientManager = ClientManager.createClient();
    }

    public String getConnectionId() {
        return connectionId;
    }

    public Mono<Void> start() {
        if (clientState.get() != WebPubSubClientState.STOPPED) {
            return Mono.error(logger.logExceptionAsError(
                new IllegalStateException("Failed to start. Client is not stopped.")));
        }
        return Mono.defer(() -> {
            boolean success = clientState.changeStateOn(WebPubSubClientState.STOPPED, WebPubSubClientState.CONNECTING);
            if (!success) {
                return Mono.error(logger.logExceptionAsError(
                    new IllegalStateException("Failed to start. Client is not stopped.")));
            } else {
                return Mono.empty();
            }
        }).then(clientAccessUriProvider.flatMap(uri -> Mono.fromCallable(() -> {
            this.endpoint = new ClientEndpoint();
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                .preferredSubprotocols(Collections.singletonList(webPubSubProtocol.getName()))
                .encoders(Collections.singletonList(MessageEncoder.class))
                .decoders(Collections.singletonList(MessageDecoder.class))
                .build();
            this.session = clientManager.connectToServer(endpoint, config, new URI(uri));
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic()))).doOnError(error -> {
            clientState.changeState(WebPubSubClientState.STOPPED);
        });
    }

    public Mono<Void> stop() {
        if (clientState.get() == WebPubSubClientState.CLOSED) {
            return Mono.error(logger.logExceptionAsError(
                new IllegalStateException("Failed to stop. Client is closed.")));
        }
        return Mono.fromCallable(() -> {
            if (session != null && session.isOpen()) {
                session.close(CloseReasons.NORMAL_CLOSURE.getCloseReason());

                handleStop();
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> closeAsync() {
        if (this.isDisposed.getAndSet(true)) {
            return this.isClosedMono.asMono();
        } else {
            return stop().then(Mono.fromRunnable(() -> {
                this.clientState.changeState(WebPubSubClientState.CLOSED);

                groupMessageEventSink.emitComplete(
                    emitFailureHandler("Unable to emit Complete to groupMessageEventSink"));
                connectedEventSink.emitComplete(
                    emitFailureHandler("Unable to emit Complete to connectedEventSink"));
                disconnectedEventSink.emitComplete(
                    emitFailureHandler("Unable to emit Complete to disconnectedEventSink"));

                isClosedMono.emitEmpty(
                    emitFailureHandler("Unable to emit Close"));
            }));
        }
    }

    public Mono<WebPubSubResult> joinGroup(String group) {
        return joinGroup(group, nextAckId());
    }

    public Mono<WebPubSubResult> joinGroup(String group, long ackId) {
        return sendMessage(new JoinGroupMessage().setGroup(group).setAckId(ackId))
            .then(waitForAckMessage(ackId));
    }

    public Mono<WebPubSubResult> leaveGroup(String group) {
        return leaveGroup(group, nextAckId());
    }

    public Mono<WebPubSubResult> leaveGroup(String group, long ackId) {
        return sendMessage(new LeaveGroupMessage().setGroup(group).setAckId(ackId))
            .then(waitForAckMessage(ackId));
    }

    public Mono<WebPubSubResult> sendToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return sendToGroup(group, content, dataType, new SendToGroupOptions().setAckId(nextAckId()));
    }

    public Mono<WebPubSubResult> sendToGroup(String group, BinaryData content, WebPubSubDataType dataType,
                                             SendToGroupOptions options) {

        long ackId = options != null && options.getAckId() != null ? options.getAckId() : nextAckId();

        BinaryData data = content;
        if (dataType == WebPubSubDataType.BINARY || dataType == WebPubSubDataType.PROTOBUF) {
            data = BinaryData.fromBytes(Base64.getEncoder().encode(content.toBytes()));
        }

        SendToGroupMessage message = new SendToGroupMessage()
            .setGroup(group)
            .setData(data)
            .setDataType(dataType.name().toLowerCase(Locale.ROOT))
            .setAckId(ackId)
            .setNoEcho(options.getNoEcho());

        Mono<Void> sendMessageMono = sendMessage(message);
        Mono<WebPubSubResult> responseMono = options.getFireAndForget()
            ? sendMessageMono.then(Mono.just(new WebPubSubResult(null)))
            : sendMessageMono.then(waitForAckMessage(ackId));
        return responseMono;
    }

    public Flux<GroupMessageEvent> receiveGroupMessageEvents() {
        return groupMessageEventSink.asFlux();
    }

    public Flux<ConnectedEvent> receiveConnectedEvents() {
        return connectedEventSink.asFlux();
    }

    public Flux<DisconnectedEvent> receiveDisconnectedEvents() {
        return disconnectedEventSink.asFlux();
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

    private Mono<Void> sendMessage(WebPubSubMessageAck message) {
        Mono<Void> verification = checkStateBeforeSend();
        return verification.then(Mono.create(sink -> {
            session.getAsyncRemote().sendObject(message, sendResult -> {
                if (sendResult.isOK()) {
                    sink.success();
                } else {
                    sink.error(logSendMessageFailedException(
                        "Failed to send message.", sendResult.getException(), true, message));
                }
            });
        }));
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
                if (m.isSuccess() || (m.getError() != null && "Duplicate".equals(m.getError().getName()))) {
                    return Mono.just(new WebPubSubResult(m.getAckId()));
                } else {
                    return Mono.error(logSendMessageFailedException(
                        "Received non-success acknowledge from the service.", null, false, ackId));
                }
            })
            // timeout or stream closed
            .timeout(Duration.ofSeconds(30), Mono.empty())
            .switchIfEmpty(Mono.error(logSendMessageFailedException(
                "Acknowledge from the service not received.", null, true, ackId)));
    }

    private void handleClose(CloseReason closeReason) {
        handleStop();
    }

    private void handleStop() {
        clientState.changeState(WebPubSubClientState.STOPPED);

        session = null;

        connectionId = null;
        reconnectionToken = null;

        ackMessageSink.emitComplete(emitFailureHandler("Unable to emit Complete to ackMessageSink"));
        ackMessageSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }

    private class ClientEndpoint extends Endpoint {

        @Override
        public void onOpen(Session session, EndpointConfig endpointConfig) {
            session.addMessageHandler(new MessageHandler.Whole<WebPubSubMessage>() {

                @Override
                public void onMessage(WebPubSubMessage webPubSubMessage) {
                    if (webPubSubMessage instanceof GroupDataMessage) {
                        groupMessageEventSink.emitNext(
                            new GroupMessageEvent((GroupDataMessage) webPubSubMessage),
                            emitFailureHandler("Unable to emit GroupMessageEvent"));
                    } else if (webPubSubMessage instanceof AckMessage) {
                        ackMessageSink.emitNext((AckMessage) webPubSubMessage,
                            emitFailureHandler("Unable to emit GroupMessageEvent"));
                    } else if (webPubSubMessage instanceof ConnectedMessage) {
                        ConnectedMessage connectedMessage = (ConnectedMessage) webPubSubMessage;
                        connectionId = connectedMessage.getConnectionId();
                        reconnectionToken = connectedMessage.getReconnectionToken();

                        logger = new ClientLogger(WebPubSubAsyncClient.class,
                            LoggingUtils.createContextWithConnectionId(connectionId));

                        connectedEventSink.emitNext(new ConnectedEvent(
                            connectionId,
                            connectedMessage.getUserId()),
                            emitFailureHandler("Unable to emit ConnectedEvent"));
                    } else if (webPubSubMessage instanceof DisconnectedMessage) {
                        disconnectedEventSink.emitNext(new DisconnectedEvent(
                            connectionId,
                            (DisconnectedMessage) webPubSubMessage),
                            emitFailureHandler("Unable to emit DisconnectedEvent"));
                    } else {
                        // TODO
                    }
                }
            });

            clientState.changeState(WebPubSubClientState.CONNECTED);
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            handleClose(closeReason);
        }

        @Override
        public void onError(Session session, Throwable thr) {
            logger.atInfo()
                .log("Error from session: " + thr.getMessage());
        }
    }

    class ClientState {

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

    private Sinks.EmitFailureHandler emitFailureHandler(String message) {
        return (signalType, emitResult) -> {
            LoggingUtils.addSignalTypeAndResult(this.logger.atWarning(), signalType, emitResult)
                .log(message);
            return false;
        };
    }

    private Mono<Void> checkStateBeforeSend() {
        Mono<Void> verification = Mono.empty();
        if (isDisposed.get()) {
            verification = Mono.error(logger.logExceptionAsError(
                new IllegalStateException("Failed to send message. WebPubSubClient is closed.")));
        }
        if (clientState.get() != WebPubSubClientState.CONNECTED) {
            verification = Mono.error(logger.logExceptionAsError(
                new IllegalStateException("Failed to send message. Client is not connected.")));
        }
        if (session == null || !session.isOpen()) {
            verification = Mono.error(logSendMessageFailedException(
                "Failed to send message. Websocket session is not opened.", null, false, (Long) null));
        }
        return verification;
    }

    private RuntimeException logSendMessageFailedException(
        String errorMessage, Throwable cause, boolean isTransient, WebPubSubMessageAck message) {

        return logSendMessageFailedException(errorMessage, cause, isTransient, message == null ? null : message.getAckId());
    }

    private RuntimeException logSendMessageFailedException(
        String errorMessage, Throwable cause, boolean isTransient, Long ackId) {

        return logger.logExceptionAsWarning(
            new SendMessageFailedException(errorMessage, cause, isTransient, ackId, null));
    }
}
