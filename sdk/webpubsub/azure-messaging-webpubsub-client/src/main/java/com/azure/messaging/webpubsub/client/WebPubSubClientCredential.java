package com.azure.messaging.webpubsub.client;

import reactor.core.publisher.Mono;

public class WebPubSubClientCredential {

    private final Mono<String> clientAccessUriProvider;

    public WebPubSubClientCredential(Mono<String> clientAccessUriProvider) {
        this.clientAccessUriProvider = clientAccessUriProvider;
    }

    public Mono<String> getClientAccessUriAsync() {
        return clientAccessUriProvider;
    }
}
