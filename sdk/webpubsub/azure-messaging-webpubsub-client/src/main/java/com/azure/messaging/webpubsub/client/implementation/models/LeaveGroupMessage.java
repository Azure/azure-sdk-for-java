// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.fasterxml.jackson.annotation.JsonGetter;

public final class LeaveGroupMessage extends WebPubSubMessageAck {

    private static final String TYPE = "leaveGroup";
    private String group;

    @JsonGetter
    public String getType() {
        return TYPE;
    }

    @JsonGetter
    public String getGroup() {
        return group;
    }

    public LeaveGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public LeaveGroupMessage setAckId(Long ackId) {
        super.setAckId(ackId);
        return this;
    }
}
