// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.messaging.webpubsub.client.models.AckResponseError;

public final class AckMessage extends WebPubSubMessage {

    private long ackId;
    private boolean success;

    private AckResponseError error;


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

    public AckResponseError getError() {
        return error;
    }

    public AckMessage setError(AckResponseError error) {
        this.error = error;
        return this;
    }
}
