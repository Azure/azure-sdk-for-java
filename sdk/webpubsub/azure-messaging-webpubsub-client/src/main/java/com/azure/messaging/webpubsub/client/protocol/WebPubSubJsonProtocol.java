// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.protocol;

public class WebPubSubJsonProtocol extends WebPubSubProtocol {
    @Override
    public boolean isReliable() {
        return false;
    }

    @Override
    public String getName() {
        return "json.webpubsub.azure.v1";
    }
}
