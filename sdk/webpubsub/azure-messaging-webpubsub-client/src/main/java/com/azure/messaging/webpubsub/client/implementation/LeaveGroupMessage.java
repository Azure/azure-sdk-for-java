// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.messaging.webpubsub.client.WebPubSubMessage;

public class LeaveGroupMessage extends WebPubSubMessage {

    private String type = "leaveGroup";
    private String group;
    private Long ackId;

    public String getGroup() {
        return group;
    }

    public LeaveGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    public Long getAckId() {
        return ackId;
    }

    public LeaveGroupMessage setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }
}
