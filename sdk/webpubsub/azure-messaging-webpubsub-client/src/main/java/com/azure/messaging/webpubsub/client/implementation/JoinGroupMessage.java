// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

public class JoinGroupMessage extends WebPubSubMessage {

    private String type = "joinGroup";
    private String group;
    private Long ackId;

    public String getGroup() {
        return group;
    }

    public JoinGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    public Long getAckId() {
        return ackId;
    }

    public JoinGroupMessage setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }
}
