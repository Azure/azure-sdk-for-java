// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

public class SendToGroupOptions {

    private Long ackId;
    private Boolean noEcho;
    private Boolean fireAndForget;

    public SendToGroupOptions() {
    }

    public Long getAckId() {
        return ackId;
    }

    public SendToGroupOptions setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }

    public Boolean getNoEcho() {
        return noEcho;
    }

    public SendToGroupOptions setNoEcho(Boolean noEcho) {
        this.noEcho = noEcho;
        return this;
    }

    public Boolean getFireAndForget() {
        return fireAndForget;
    }

    public SendToGroupOptions setFireAndForget(Boolean fireAndForget) {
        this.fireAndForget = fireAndForget;
        return this;
    }
}
