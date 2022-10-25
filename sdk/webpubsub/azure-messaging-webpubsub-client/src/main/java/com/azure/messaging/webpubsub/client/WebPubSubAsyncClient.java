package com.azure.messaging.webpubsub.client;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.MessageEncoder;
import com.azure.messaging.webpubsub.client.message.JoinGroupMessage;
import com.azure.messaging.webpubsub.client.message.LeaveGroupMessage;
import com.azure.messaging.webpubsub.client.message.SendToGroupMessage;
import com.azure.messaging.webpubsub.client.message.WebPubSubMessage;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.core.CloseReasons;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.Future;

public class WebPubSubAsyncClient {

    private final String url;

    private final ClientManager clientManager;

    private Endpoint endpoint;

    private Session session;

    private Sinks.Many<WebPubSubMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();

    public WebPubSubAsyncClient(String url) {
        this.url = url;
        this.clientManager = ClientManager.createClient();
    }

    public Mono<Void> start() throws URISyntaxException, DeploymentException {
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
            .preferredSubprotocols(Collections.singletonList("json.webpubsub.azure.v1"))
            .encoders(Collections.singletonList(MessageEncoder.class))
            .decoders(Collections.singletonList(MessageDecoder.class))
            .build();
        endpoint = new ClientEndpoint();
        Future<Session> sessionFuture = clientManager.asyncConnectToServer(endpoint, config, new URI(url));

        return Mono.fromCallable(() -> {
            this.session = sessionFuture.get();
            return session;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> close() throws IOException {
        return Mono.fromCallable(() -> {
            if (session != null && session.isOpen()) {
                session.close(CloseReasons.NORMAL_CLOSURE.getCloseReason());
                messageSink.tryEmitComplete();
            }
            return (Void) null;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> joinGroup(JoinGroupMessage message) {
        return Mono.fromCallable(() -> {
            return session.getAsyncRemote().sendObject(message).get();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> leaveGroup(LeaveGroupMessage message) {
        return Mono.fromCallable(() -> {
            return session.getAsyncRemote().sendObject(message).get();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> sendMessageToGroup(SendToGroupMessage message) {
        return Mono.fromCallable(() -> {
            return session.getAsyncRemote().sendObject(message).get();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    Flux<WebPubSubMessage> getMessages() {
        return messageSink.asFlux();
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
