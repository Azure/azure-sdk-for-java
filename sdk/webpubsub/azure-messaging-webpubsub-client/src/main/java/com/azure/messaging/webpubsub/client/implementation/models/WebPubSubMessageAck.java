// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.fasterxml.jackson.annotation.JsonGetter;

public abstract class WebPubSubMessageAck extends WebPubSubMessage {
    private Long ackId;

    @JsonGetter
    public Long getAckId() {
        return ackId;
    }

    public WebPubSubMessageAck setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }
}
