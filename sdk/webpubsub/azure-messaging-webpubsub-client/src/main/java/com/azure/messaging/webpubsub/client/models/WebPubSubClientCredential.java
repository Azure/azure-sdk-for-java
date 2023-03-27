// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;
import reactor.core.publisher.Mono;

/**
 * The credential for WebPubSub client.
 */
@Immutable
public final class WebPubSubClientCredential {

    private final Mono<String> clientAccessUrlProvider;

    /**
     * Creates a new instance of WebPubSubClientCredential.
     *
     * @param clientAccessUrlProvider the provider for client access URL.
     */
    public WebPubSubClientCredential(Mono<String> clientAccessUrlProvider) {
        this.clientAccessUrlProvider = clientAccessUrlProvider;
    }

    /**
     * Gets the provider for client access URL.
     *
     * @return the provider for client access URL.
     */
    public Mono<String> getClientAccessUrl() {
        return clientAccessUrlProvider;
    }
}
