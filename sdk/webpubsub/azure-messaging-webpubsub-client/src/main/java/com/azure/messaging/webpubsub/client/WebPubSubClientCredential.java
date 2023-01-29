// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import reactor.core.publisher.Mono;

/**
 * The credential for WebPubSub client.
 */
public class WebPubSubClientCredential {

    private final Mono<String> clientAccessUriProvider;

    /**
     * Creates a new instance of WebPubSubClientCredential.
     *
     * @param clientAccessUriProvider the provider for client access URI.
     */
    public WebPubSubClientCredential(Mono<String> clientAccessUriProvider) {
        this.clientAccessUriProvider = clientAccessUriProvider;
    }

    /**
     * Gets the provider for client access URI.
     *
     * @return the provider for client access URI.
     */
    public Mono<String> getClientAccessUriAsync() {
        return clientAccessUriProvider;
    }
}
