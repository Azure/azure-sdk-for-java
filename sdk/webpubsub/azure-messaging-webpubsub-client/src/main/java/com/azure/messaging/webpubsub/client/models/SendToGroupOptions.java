// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Fluent;

@Fluent
public final class SendToGroupOptions {

    private Long ackId;
    private Boolean noEcho = false;
    private Boolean fireAndForget = false;

    public SendToGroupOptions() {
    }

    public Long getAckId() {
        return ackId;
    }

    public SendToGroupOptions setAckId(long ackId) {
        this.ackId = ackId;
        return this;
    }

    public Boolean getNoEcho() {
        return noEcho;
    }

    public SendToGroupOptions setNoEcho(boolean noEcho) {
        this.noEcho = noEcho;
        return this;
    }

    public Boolean getFireAndForget() {
        return fireAndForget;
    }

    public SendToGroupOptions setFireAndForget(boolean fireAndForget) {
        this.fireAndForget = fireAndForget;
        return this;
    }
}
