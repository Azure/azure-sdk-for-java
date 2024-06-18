// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Fluent;

@Fluent
public final class LeaveGroupMessage extends WebPubSubMessageAck {

    private static final String TYPE = "leaveGroup";
    private String group;

    public String getType() {
        return TYPE;
    }

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
