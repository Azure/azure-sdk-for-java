// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Fluent;

@Fluent
public final class SendEventOptions {

    private Long ackId;
    private Boolean fireAndForget = false;

    public SendEventOptions() {
    }

    public Long getAckId() {
        return ackId;
    }

    public SendEventOptions setAckId(long ackId) {
        this.ackId = ackId;
        return this;
    }

    public boolean getFireAndForget() {
        return fireAndForget;
    }

    public SendEventOptions setFireAndForget(boolean fireAndForget) {
        this.fireAndForget = fireAndForget;
        return this;
    }
}
