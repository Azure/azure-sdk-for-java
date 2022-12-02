// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

import com.azure.core.util.BinaryData;

public class GroupDataMessage extends WebPubSubMessage {

    protected BinaryData data;
    protected WebPubSubDataType dataType;
    protected String fromUserId;
    protected String group;
    protected Long sequenceId;

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
