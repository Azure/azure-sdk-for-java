// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.protocol;

/**
 * The protocol of json.reliable.webpubsub.azure.v1
 */
public class WebPubSubJsonReliableProtocol extends WebPubSubProtocol {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReliable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "json.reliable.webpubsub.azure.v1";
    }
}
