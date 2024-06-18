// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

public final class SequenceAckMessage extends WebPubSubMessage {

    private static final String TYPE = "sequenceAck";

    private long sequenceId = 0L;

    public String getType() {
        return TYPE;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public SequenceAckMessage setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }
}
