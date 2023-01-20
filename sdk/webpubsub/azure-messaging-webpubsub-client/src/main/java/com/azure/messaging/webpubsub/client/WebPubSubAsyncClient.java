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
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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
        this.endpoint = new ClientEndpoint();
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
            .preferredSubprotocols(Collections.singletonList(webPubSubProtocol.getName()))
            .encoders(Collections.singletonList(MessageEncoder.class))
            .decoders(Collections.singletonList(MessageDecoder.class))
            .build();

        return clientAccessUriProvider.flatMap(uri -> Mono.fromCallable(() -> {
            this.session = clientManager.connectToServer(endpoint, config, new URI(uri));
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<Void> stop() {
        return Mono.fromCallable(() -> {
            if (session != null && session.isOpen()) {
                session.close(CloseReasons.NORMAL_CLOSURE.getCloseReason());

                session = null;

//                groupDataMessageSink.tryEmitComplete();
//                groupDataMessageSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
                ackMessageSink.emitComplete(emitFailureHandler("Unable to emit Complete to ackMessageSink"));
                ackMessageSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

//                disconnectedEventSink.tryEmitNext(new DisconnectedEvent(connectionId, ""));
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> closeAsync() {
        if (this.isDisposed.getAndSet(true)) {
            return this.isClosedMono.asMono();
        } else {
            return stop().then(Mono.fromRunnable(() -> {
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
        if (session == null || !session.isOpen()) {
            return Mono.error(logSendMessageFailedException(
                "Failed to send message. Websocket session is not opened.", null, message));
        }
        return Mono.create(sink -> {
            session.getAsyncRemote().sendObject(message, sendResult -> {
                if (sendResult.isOK()) {
                    sink.success();
                } else {
                    sink.error(logSendMessageFailedException(
                        "Failed to send message.", sendResult.getException(), message));
                }
            });
        });
    }

    private Mono<WebPubSubResult> waitForAckMessage(long ackId) {
        return receiveAckMessages()
            .filter(m -> ackId == m.getAckId())
            .map(m -> new WebPubSubResult(m.getAckId()))
            .next()
            // error handling
            .switchIfEmpty(Mono.error(new RuntimeException()));
    }

    private class ClientEndpoint extends Endpoint {

        @Override
        public void onOpen(Session session, EndpointConfig endpointConfig) {
//            System.out.println("session open");

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
                        connectionId = ((ConnectedMessage) webPubSubMessage).getConnectionId();

                        logger = new ClientLogger(WebPubSubAsyncClient.class,
                            LoggingUtils.createContextWithConnectionId(connectionId));

                        connectedEventSink.emitNext(new ConnectedEvent(
                            connectionId,
                            ((ConnectedMessage) webPubSubMessage).getUserId()),
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
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
//            System.out.println("session close: " + closeReason);
        }

        @Override
        public void onError(Session session, Throwable thr) {
//            System.out.println("session error: " + thr);
        }
    }

    private Sinks.EmitFailureHandler emitFailureHandler(String message) {
        return (signalType, emitResult) -> {
            LoggingUtils.addSignalTypeAndResult(this.logger.atWarning(), signalType, emitResult)
                .log(message);
            return false;
        };
    }

    private RuntimeException logSendMessageFailedException(
        String errorMessage, Throwable cause, WebPubSubMessageAck message) {

        return logger.logExceptionAsWarning(
            new SendMessageFailedException(errorMessage, cause, message.getAckId(), null));
    }
}
