// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

@Immutable
public final class WebPubSubResult {

    private final Long ackId;

    public WebPubSubResult(Long ackId) {
        this.ackId = ackId;
    }

    public Long getAckId() {
        return ackId;
    }
}
