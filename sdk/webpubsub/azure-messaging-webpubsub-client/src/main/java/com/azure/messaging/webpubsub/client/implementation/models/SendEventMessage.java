// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.fasterxml.jackson.annotation.JsonGetter;

public final class SendEventMessage extends WebPubSubMessageAck {

    private static final String TYPE = "event";

    private String event;

    private String dataType;

    private Object data;

    @JsonGetter
    public String getType() {
        return TYPE;
    }

    @JsonGetter
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

    @JsonGetter
    public String getDataType() {
        return dataType;
    }

    public SendEventMessage setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    @JsonGetter
    public Object getData() {
        return data;
    }

    public SendEventMessage setData(Object data) {
        this.data = data;
        return this;
    }
}
