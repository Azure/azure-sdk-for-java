// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.implementation.AckMessage;
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

    private Sinks.Many<WebPubSubMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();

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
                messageSink.tryEmitComplete();
                messageSink = Sinks.many().multicast().onBackpressureBuffer();
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<WebPubSubResult> joinGroup(String group) {
        return joinGroup(group, nextAckId());
    }

    public Mono<WebPubSubResult> joinGroup(String group, long ackId) {
        return Mono.fromCallable(() -> {
            session.getBasicRemote().sendObject(new JoinGroupMessage().setGroup(group).setAckId(ackId));
            return new WebPubSubResult();
        }).subscribeOn(Schedulers.boundedElastic()).then(waitForAckMessage(ackId));
    }

    public Mono<WebPubSubResult> leaveGroup(String group) {
        return leaveGroup(group, nextAckId());
    }

    public Mono<WebPubSubResult> leaveGroup(String group, long ackId) {
        return Mono.fromCallable(() -> {
            session.getBasicRemote().sendObject(new LeaveGroupMessage().setGroup(group).setAckId(ackId));
            return new WebPubSubResult();
        }).subscribeOn(Schedulers.boundedElastic()).then(waitForAckMessage(ackId));
    }

    public Mono<WebPubSubResult> sendMessageToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return sendMessageToGroup(group, content, dataType, nextAckId(), false, false);
    }

    public Mono<WebPubSubResult> sendMessageToGroup(String group, BinaryData content, WebPubSubDataType dataType,
                                                    long ackId, boolean noEcho, boolean fireAndForget) {

        Mono<WebPubSubResult> sendMono = Mono.fromCallable(() -> {
            BinaryData data = content;
            if (dataType == WebPubSubDataType.BINARY) {
                data = BinaryData.fromBytes(Base64.getEncoder().encode(content.toBytes()));
            }

            session.getBasicRemote().sendObject(new SendToGroupMessage()
                .setGroup(group)
                .setData(data)
                .setDataType(dataType.name().toLowerCase(Locale.ROOT))
                .setAckId(ackId)
                .setNoEcho(noEcho));
            return (WebPubSubResult) null;
        }).subscribeOn(Schedulers.boundedElastic());

        if (!fireAndForget) {
            sendMono = sendMono.then(waitForAckMessage(ackId));
        } else {
            sendMono = sendMono.then(Mono.just(new WebPubSubResult()));
        }
        return sendMono;
    }

    public Flux<GroupDataMessage> receiveGroupMessages() {
        return messageSink.asFlux().filter(m -> m instanceof GroupDataMessage).cast(GroupDataMessage.class);
    }

    private static final AtomicLong ACK_ID = new AtomicLong(0);
    private long nextAckId() {
        return ACK_ID.getAndIncrement();
    }

    private Flux<AckMessage> receiveAckMessages() {
        return messageSink.asFlux().filter(m -> m instanceof AckMessage).cast(AckMessage.class);
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
                    messageSink.tryEmitNext(webPubSubMessage);
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
