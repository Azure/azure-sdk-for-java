// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonGetter;

public final class SendToGroupMessage extends WebPubSubMessageAck {

    private static final String TYPE = "sendToGroup";

    private String group;

    private Boolean noEcho = false;

    private String dataType;

    private BinaryData data;

    @JsonGetter
    public String getType() {
        return TYPE;
    }

    @JsonGetter
    public String getGroup() {
        return group;
    }

    public SendToGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public SendToGroupMessage setAckId(long ackId) {
        super.setAckId(ackId);
        return this;
    }

    @JsonGetter
    public Boolean isNoEcho() {
        return noEcho;
    }

    public SendToGroupMessage setNoEcho(Boolean noEcho) {
        this.noEcho = noEcho;
        return this;
    }

    @JsonGetter
    public String getDataType() {
        return dataType;
    }

    public SendToGroupMessage setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    @JsonGetter
    public BinaryData getData() {
        return data;
    }

    public SendToGroupMessage setData(BinaryData data) {
        this.data = data;
        return this;
    }
}
