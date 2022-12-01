// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.BinaryData;
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
import java.util.Collections;
import java.util.Locale;

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

    public Mono<Void> close() {
        return Mono.fromCallable(() -> {
            if (session != null && session.isOpen()) {
                session.close(CloseReasons.NORMAL_CLOSURE.getCloseReason());
                messageSink.tryEmitComplete();
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<WebPubSubResult> joinGroup(String group) {
        return Mono.fromCallable(() -> {
            session.getBasicRemote().sendObject(new JoinGroupMessage().setGroup(group));
            return (WebPubSubResult) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<WebPubSubResult> joinGroup(String group, long ackId) {
        return Mono.fromCallable(() -> {
            session.getBasicRemote().sendObject(new JoinGroupMessage().setGroup(group).setAckId(ackId));
            return (WebPubSubResult) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<WebPubSubResult> leaveGroup(String group) {
        return Mono.fromCallable(() -> {
            session.getBasicRemote().sendObject(new LeaveGroupMessage().setGroup(group));
            return (WebPubSubResult) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<WebPubSubResult> leaveGroup(String group, long ackId) {
        return Mono.fromCallable(() -> {
            session.getBasicRemote().sendObject(new LeaveGroupMessage().setGroup(group).setAckId(ackId));
            return (WebPubSubResult) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<WebPubSubResult> sendMessageToGroup(String group, BinaryData content, WebPubSubDataType dataType) {
        return Mono.fromCallable(() -> {
            session.getBasicRemote().sendObject(new SendToGroupMessage()
                .setGroup(group)
                .setData(content)
                .setDataType(dataType.name().toLowerCase(Locale.ROOT)));
            return (WebPubSubResult) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<WebPubSubResult> sendMessageToGroup(String group, BinaryData content, WebPubSubDataType dataType,
                                                    long ackId, boolean noEcho, boolean fireAndForget) {
        return Mono.fromCallable(() -> {
            session.getBasicRemote().sendObject(new SendToGroupMessage()
                .setGroup(group)
                .setData(content)
                .setDataType(dataType.name().toLowerCase(Locale.ROOT))
                .setAckId(ackId)
                .setNoEcho(noEcho));
            return (WebPubSubResult) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<GroupDataMessage> receiveGroupMessages() {
        return messageSink.asFlux().filter(m -> m instanceof GroupDataMessage).cast(GroupDataMessage.class);
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
