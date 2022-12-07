// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.implementation.AckMessage;
import com.azure.messaging.webpubsub.client.implementation.ConnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.DisconnectedEvent;
import com.azure.messaging.webpubsub.client.implementation.DisconnectedMessage;
import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.MessageEncoder;
import com.azure.messaging.webpubsub.client.implementation.JoinGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.LeaveGroupMessage;
import com.azure.messaging.webpubsub.client.implementation.SendToGroupMessage;
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
import java.util.concurrent.atomic.AtomicLong;

@ServiceClient(builder = WebPubSubClientBuilder.class)
public class WebPubSubAsyncClient {

    private final Mono<String> clientAccessUriProvider;

    private final ClientManager clientManager;

    private Endpoint endpoint;

    private Session session;

    private Sinks.Many<GroupDataMessage> groupDataMessageSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<AckMessage> ackMessageSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<ConnectedEvent> connectedEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    private Sinks.Many<DisconnectedEvent> disconnectedEventSink =
        Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    WebPubSubAsyncClient(Mono<String> clientAccessUriProvider) {
        this.clientAccessUriProvider = clientAccessUriProvider;

        this.clientManager = ClientManager.createClient();
    }

    public Mono<Void> start() {
        this.endpoint = new ClientEndpoint();
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
            .preferredSubprotocols(Collections.singletonList("json.webpubsub.azure.v1"))
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

                groupDataMessageSink.tryEmitComplete();
                groupDataMessageSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
                ackMessageSink.tryEmitComplete();
                ackMessageSink = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
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

    public Mono<WebPubSubResult> sendMessageToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return sendMessageToGroup(group, content, dataType, nextAckId(), false, false);
    }

    public Mono<WebPubSubResult> sendMessageToGroup(String group, BinaryData content, WebPubSubDataType dataType,
                                                    long ackId, boolean noEcho, boolean fireAndForget) {

        BinaryData data = content;
        if (dataType == WebPubSubDataType.BINARY) {
            data = BinaryData.fromBytes(Base64.getEncoder().encode(content.toBytes()));
        }

        SendToGroupMessage message = new SendToGroupMessage()
            .setGroup(group)
            .setData(data)
            .setDataType(dataType.name().toLowerCase(Locale.ROOT))
            .setAckId(ackId)
            .setNoEcho(noEcho);

        Mono<Void> sendMessageMono = sendMessage(message);
        Mono<WebPubSubResult> responseMono = fireAndForget
            ? sendMessageMono.then(Mono.just(new WebPubSubResult()))
            : sendMessageMono.then(waitForAckMessage(ackId));
        return responseMono;
    }

    public Flux<GroupDataMessage> receiveGroupMessages() {
        return groupDataMessageSink.asFlux();
    }

    public Flux<ConnectedEvent> receiveConnectedEvents() {
        return connectedEventSink.asFlux();
    }

    public Flux<DisconnectedEvent> receiveDisconnectedEvents() {
        return disconnectedEventSink.asFlux();
    }

    private static final AtomicLong ACK_ID = new AtomicLong(0);
    private long nextAckId() {
        return ACK_ID.getAndIncrement();
    }

    private Flux<AckMessage> receiveAckMessages() {
        return ackMessageSink.asFlux();
    }

    private Mono<Void> sendMessage(WebPubSubMessage message) {
        return Mono.create(sink -> {
            session.getAsyncRemote().sendObject(message, sendResult -> {
                if (sendResult.isOK()) {
                    sink.success();
                } else {
                    sink.error(sendResult.getException());
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
            System.out.println("session open");

            session.addMessageHandler(new MessageHandler.Whole<WebPubSubMessage>() {

                @Override
                public void onMessage(WebPubSubMessage webPubSubMessage) {
                    if (webPubSubMessage instanceof GroupDataMessage) {
                        groupDataMessageSink.tryEmitNext((GroupDataMessage) webPubSubMessage);
                    } else if (webPubSubMessage instanceof AckMessage) {
                        ackMessageSink.tryEmitNext((AckMessage) webPubSubMessage);
                    } else if (webPubSubMessage instanceof ConnectedMessage) {
                        connectedEventSink.tryEmitNext(new ConnectedEvent(
                            ((ConnectedMessage) webPubSubMessage).getConnectionId(),
                            ((ConnectedMessage) webPubSubMessage).getUserId()));
                    } else if (webPubSubMessage instanceof DisconnectedMessage) {
                        disconnectedEventSink.tryEmitNext(new DisconnectedEvent(
                            ((DisconnectedMessage) webPubSubMessage).getReason()));
                    } else {
                        // TODO
                    }
                }
            });
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            System.out.println("session close: " + closeReason);
        }

        @Override
        public void onError(Session session, Throwable thr) {
            System.out.println("session error: " + thr);
        }
    }
}
