// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.messaging.webpubsub.client.models.AckMessageError;

public final class AckMessage extends WebPubSubMessage {

    private long ackId;
    private boolean success;

    private AckMessageError error;


    public long getAckId() {
        return ackId;
    }

    public AckMessage setAckId(long ackId) {
        this.ackId = ackId;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public AckMessage setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public AckMessageError getError() {
        return error;
    }

    public AckMessage setError(AckMessageError error) {
        this.error = error;
        return this;
    }
}
