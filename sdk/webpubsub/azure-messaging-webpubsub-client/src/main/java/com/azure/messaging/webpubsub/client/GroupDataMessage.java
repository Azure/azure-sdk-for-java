// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;

public class GroupDataMessage extends WebPubSubMessage {

    private BinaryData data;
    private WebPubSubDataType dataType;
    private String fromUserId;
    private String group;
    private Long sequenceId;

    public GroupDataMessage setData(BinaryData data) {
        this.data = data;
        return this;
    }

    public GroupDataMessage setDataType(WebPubSubDataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public GroupDataMessage setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
        return this;
    }

    public GroupDataMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    public GroupDataMessage setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
        return this;
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
