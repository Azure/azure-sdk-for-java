// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

public class WebPubSubResult {

    private final Long ackId;

    WebPubSubResult() {
        this.ackId = null;
    }

    WebPubSubResult(long ackId) {
        this.ackId = ackId;
    }

    public Long getAckId() {
        return ackId;
    }
}
