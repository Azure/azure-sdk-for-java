// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.messaging.webpubsub.client.models.WebPubSubMessage;

public final class SequenceAckMessage extends WebPubSubMessage {

    private final String type = "sequenceAck";

    private long sequenceId = 0L;

    public long getSequenceId() {
        return sequenceId;
    }

    public SequenceAckMessage setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }
}
