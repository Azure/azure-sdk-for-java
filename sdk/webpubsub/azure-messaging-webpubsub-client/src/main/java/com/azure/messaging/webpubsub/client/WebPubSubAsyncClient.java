package com.azure.messaging.webpubsub.client;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import com.azure.messaging.webpubsub.client.implementation.MessageDecoder;
import com.azure.messaging.webpubsub.client.implementation.MessageEncoder;
import com.azure.messaging.webpubsub.client.models.SendToGroupMessage;
import org.glassfish.tyrus.client.ClientManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.Future;

public class WebPubSubAsyncClient {

    private final String url;

    private final ClientManager clientManager;

    private Endpoint endpoint;

    private Session session;

    public WebPubSubAsyncClient(String url) {
        this.url = url;
        this.clientManager = ClientManager.createClient();
    }

    public Mono<Void> startAsync() throws URISyntaxException, DeploymentException {
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
            .preferredSubprotocols(Collections.singletonList("json.webpubsub.azure.v1"))
            .encoders(Collections.singletonList(MessageEncoder.class))
            .decoders(Collections.singletonList(MessageDecoder.class))
            .build();
        Future<Session> sessionFuture = clientManager.asyncConnectToServer(new ClientEndpoint(), new URI(url));

        return Mono.fromCallable(() -> {
            this.session = sessionFuture.get();
            return session;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> sendMessageToGroupAsync(SendToGroupMessage message) {
        return Mono.fromCallable(() -> {
            return session.getAsyncRemote().sendObject(message).get();
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private static class ClientEndpoint extends Endpoint {

        @Override
        public void onOpen(Session session, EndpointConfig endpointConfig) {

        }
    }
}
