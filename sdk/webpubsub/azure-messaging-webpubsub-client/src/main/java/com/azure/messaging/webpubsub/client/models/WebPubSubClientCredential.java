// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

import java.util.function.Supplier;

/**
 * The credential for WebPubSub client.
 */
@Immutable
public final class WebPubSubClientCredential {

    private final Supplier<String> clientAccessUrlSupplier;

    /**
     * Creates a new instance of WebPubSubClientCredential.
     *
     * @param clientAccessUrlSupplier the supplier of client access URL.
     */
    public WebPubSubClientCredential(Supplier<String> clientAccessUrlSupplier) {
        this.clientAccessUrlSupplier = clientAccessUrlSupplier;
    }

    /**
     * Gets the supplier of client access URL.
     *
     * @return the supplier of client access URL.
     */
    public Supplier<String> getClientAccessUrlSupplier() {
        return clientAccessUrlSupplier;
    }
}
