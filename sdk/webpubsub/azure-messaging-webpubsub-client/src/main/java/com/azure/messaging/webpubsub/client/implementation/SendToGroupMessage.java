// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.BinaryData;

public final class SendToGroupMessage extends WebPubSubMessageAck {

    private final String type = "sendToGroup";

    private String group;

    private Boolean noEcho = false;

    private String dataType;

    private BinaryData data;

    public String getGroup() {
        return group;
    }

    public SendToGroupMessage setGroup(String group) {
        this.group = group;
        return this;
    }

    @Override
    public SendToGroupMessage setAckId(Long ackId) {
        super.setAckId(ackId);
        return this;
    }

    public Boolean isNoEcho() {
        return noEcho;
    }

    public SendToGroupMessage setNoEcho(Boolean noEcho) {
        this.noEcho = noEcho;
        return this;
    }

    public String getDataType() {
        return dataType;
    }

    public SendToGroupMessage setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public BinaryData getData() {
        return data;
    }

    public SendToGroupMessage setData(BinaryData data) {
        this.data = data;
        return this;
    }
}
