// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

public final class JoinGroupMessage extends WebPubSubMessageAck {

    private final String type = "joinGroup";
    private String group;

    public String getGroup() {
        return group;
    }

    public JoinGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public JoinGroupMessage setAckId(Long ackId) {
        super.setAckId(ackId);
        return this;
    }
}
