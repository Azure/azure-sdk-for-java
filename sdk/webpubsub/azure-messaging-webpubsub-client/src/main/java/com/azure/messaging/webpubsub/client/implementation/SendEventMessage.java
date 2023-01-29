// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;

public final class SendEventMessage extends WebPubSubMessageAck {

    private final String type = "event";

    private String event;

    private String dataType;

    private BinaryData data;

    public String getEvent() {
        return event;
    }

    public SendEventMessage setEvent(String event) {
        this.event = event;
        return this;
    }

    @Override
    public SendEventMessage setAckId(Long ackId) {
        super.setAckId(ackId);
        return this;
    }

    public String getDataType() {
        return dataType;
    }

    public SendEventMessage setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public BinaryData getData() {
        return data;
    }

    public SendEventMessage setData(BinaryData data) {
        this.data = data;
        return this;
    }
}
