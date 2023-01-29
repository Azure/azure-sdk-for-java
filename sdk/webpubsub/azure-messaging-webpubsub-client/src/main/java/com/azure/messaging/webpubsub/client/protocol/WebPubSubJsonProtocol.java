// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.protocol;

/**
 * The protocol of json.webpubsub.azure.v1
 */
public class WebPubSubJsonProtocol extends WebPubSubProtocol {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReliable() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "json.webpubsub.azure.v1";
    }
}
