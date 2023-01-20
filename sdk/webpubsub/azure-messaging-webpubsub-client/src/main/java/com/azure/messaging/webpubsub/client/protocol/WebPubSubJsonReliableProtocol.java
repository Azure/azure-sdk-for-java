// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.protocol;

public class WebPubSubJsonReliableProtocol extends WebPubSubProtocol {
    @Override
    public boolean isReliable() {
        return true;
    }

    @Override
    public String getName() {
        return "json.reliable.webpubsub.azure.v1";
    }
}
