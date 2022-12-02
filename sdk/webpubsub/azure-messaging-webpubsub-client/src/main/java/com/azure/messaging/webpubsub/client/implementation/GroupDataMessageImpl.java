// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;
import com.azure.messaging.webpubsub.client.GroupDataMessage;
import com.azure.messaging.webpubsub.client.WebPubSubDataType;

public class GroupDataMessageImpl extends GroupDataMessage {

    public GroupDataMessageImpl setData(BinaryData data) {
        this.data = data;
        return this;
    }

    public GroupDataMessageImpl setDataType(WebPubSubDataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public GroupDataMessageImpl setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }

    public GroupDataMessageImpl setGroup(String group) {
        this.group = group;
        return this;
    }

    public GroupDataMessageImpl setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
        return this;
    }
}
