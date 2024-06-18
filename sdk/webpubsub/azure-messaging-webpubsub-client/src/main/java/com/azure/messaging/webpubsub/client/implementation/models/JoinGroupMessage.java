// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Fluent;

@Fluent
public final class JoinGroupMessage extends WebPubSubMessageAck {

    private static final String TYPE = "joinGroup";
    private String group;

    public String getType() {
        return TYPE;
    }

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
