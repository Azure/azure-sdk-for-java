// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

@Immutable
public final class GroupDataMessage extends WebPubSubMessage {

    private final String group;
    private final WebPubSubDataType dataType;
    private final BinaryData data;
    private final String fromUserId;
    private final Long sequenceId;

    public GroupDataMessage(String group, WebPubSubDataType dataType, BinaryData data, String fromUserId,
                            Long sequenceId) {
        this.data = data;
        this.dataType = dataType;
        this.fromUserId = fromUserId;
        this.group = group;
        this.sequenceId = sequenceId;
    }

    public BinaryData getData() {
        return data;
    }

    public WebPubSubDataType getDataType() {
        return dataType;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getGroup() {
        return group;
    }

    public Long getSequenceId() {
        return sequenceId;
    }
}
