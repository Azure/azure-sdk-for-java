// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

/**
 * The superclass of protocol for WebPubSub client.
 */
public abstract class WebPubSubProtocol {

    /**
     * Gets whether the protocol is reliable.
     *
     * @return whether the protocol is reliable.
     */
    public abstract boolean isReliable();

    /**
     * Gets the name of the protocol.
     *
     * @return the name of the protocol.
     */
    public abstract String getName();
}
